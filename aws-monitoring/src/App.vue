<template>
  <div id="app">
    <h1>AWS 인스턴스 모니터링</h1>

    <div v-if="loading" class="loading">로딩 중...</div>

    <div v-if="error" class="error">
      <p>API 요청 중 오류가 발생했습니다: {{ error }}</p>
    </div>

    <div class="select-container">
      <h3>이미지 선택</h3>
      <select v-model="selectedImageId" class="image-select">
        <option v-for="image in images" :key="image.imageId" :value="image.imageId">
          {{ image.imageName }} ({{ image.imageDescription }})
        </option>
      </select>
    </div>

    <div v-if="instances && !loading" class="instances-container">
      <h2>인스턴스 리스트
        <button @click="fetchInstances" class="refresh-btn">새로고침</button>
      </h2>
      <table class="instances-table">
        <thead>
        <tr>
          <th>인스턴스 ID</th>
          <th>상태</th>
          <th>작업</th>
          <th>Condor Status</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="instance in instances" :key="instance.instanceId">
          <td>{{ instance.instanceId }}</td>
          <td>{{ instance.instanceState }}</td>
          <td>
            <button
                class="btn start-btn"
                @click="startInstance(instance.instanceId)"
                :disabled="instance.instanceState === 'running'"
            >
              시작
            </button>
            <button
                class="btn stop-btn"
                @click="stopInstance(instance.instanceId)"
                :disabled="instance.instanceState === 'stopped'"
            >
              정지
            </button>
            <button class="btn reboot-btn" @click="rebootInstance(instance.instanceId)">
              재부팅
            </button>
          </td>
          <td>
            <button
                class="btn condor-start-btn"
                @click="startCondor(instance.instancePublicDnsName, instance.instanceId)">
              Condor Start
            </button>
            <button
                class="btn condor-btn"
                @click="condorStatus(instance.instancePublicDnsName, instance.instanceId)"
            >
              상태 확인
            </button>
            <div v-if="instance.condorStatus" class="condor-status-container">
              <table class="condor-details">
                <tr>
                  <th>이름</th>
                  <td>{{ instance.condorStatus.details.name }}</td>
                </tr>
                <tr>
                  <th>OpSys</th>
                  <td>{{ instance.condorStatus.details.opSys }}</td>
                </tr>
                <tr>
                  <th>Arch</th>
                  <td>{{ instance.condorStatus.details.arch }}</td>
                </tr>
                <tr>
                  <th>상태</th>
                  <td>{{ instance.condorStatus.details.state }}</td>
                </tr>
                <tr>
                  <th>활동</th>
                  <td>{{ instance.condorStatus.details.activity }}</td>
                </tr>
                <tr>
                  <th>LoadAv</th>
                  <td>{{ instance.condorStatus.details.loadAverage }}</td>
                </tr>
                <tr>
                  <th>Mem</th>
                  <td>{{ instance.condorStatus.details.memory }}</td>
                </tr>
                <tr>
                  <th>ActvtyTime</th>
                  <td>{{ instance.condorStatus.details.activityTime }}</td>
                </tr>
              </table>
              <p> {{ instance.condorStatus.header }} <br> {{ instance.condorStatus.x86_64 }} <br> {{ instance.condorStatus.total }}</p>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="create-instance-container">
      <button @click="createInstance" class="create-instance-btn">인스턴스 생성</button>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      images: [],
      selectedImageId: null,
      instances: [],
      loading: true,
      error: null,
    };
  },
  mounted() {
    this.fetchImages();
    this.fetchInstances();
  },
  methods: {
    async fetchImages() {
      try {
        const response = await axios.get("http://localhost:8080/api/v1/aws/images");
        this.images = response.data;
      } catch (err) {
        this.error = err.message;
      }
    },
    async fetchInstances() {
      try {
        const response = await axios.get("http://localhost:8080/api/v1/aws/instances");
        this.instances = response.data.map((instance) => ({
          ...instance,
          condorStatus: null, // condorStatus 상태 초기화
        }));
        this.loading = false;
      } catch (err) {
        this.error = err.message;
        this.loading = false;
      }
    },
    async startInstance(instanceId) {
      try {
        await axios.post("http://localhost:8080/api/v1/aws/start", { instanceId });
        this.fetchInstances();
      } catch (err) {
        alert(`인스턴스를 시작할 수 없습니다: ${err.message}`);
      }
    },
    async stopInstance(instanceId) {
      try {
        await axios.post("http://localhost:8080/api/v1/aws/stop", { instanceId });
        this.fetchInstances();
      } catch (err) {
        alert(`인스턴스를 정지할 수 없습니다: ${err.message}`);
      }
    },
    async rebootInstance(instanceId) {
      try {
        await axios.post("http://localhost:8080/api/v1/aws/reboot", { instanceId });
        this.fetchInstances();
      } catch (err) {
        alert(`인스턴스를 재부팅할 수 없습니다: ${err.message}`);
      }
    },
    async createInstance() {
      if (!this.selectedImageId) {
        alert("인스턴스를 생성하려면 이미지를 선택해야 합니다.");
        return;
      }

      try {
        await axios.post("http://localhost:8080/api/v1/aws/create", {
          instanceAmi: this.selectedImageId,
        });
        this.fetchInstances();
      } catch (err) {
        alert(`인스턴스를 생성할 수 없습니다: ${err.message}`);
      }
    },
      async startCondor(host, instanceId) {
        try {
          const response = await axios.post('http://localhost:8080/api/v1/aws/condor/start', {
            host: host
          });
          console.log(response.data);
          alert(`Condor 상태 시작됨: ${instanceId}`);
        } catch (error) {
          console.error('Condor 시작 실패:', error);
          alert('Condor 시작 실패');
        }
      },
    async condorStatus(host, instanceId) {
      try {
        const response = await axios.post("http://localhost:8080/api/v1/aws/condor/status", {
          host,
        });

        const instance = this.instances.find((inst) => inst.instanceId === instanceId);
        if (instance) {
          const result = response.data.result;

          if (!result) {
            console.error("Condor status 결과가 없습니다.");
            return;
          }

          const lines = result.split("\n").filter((line) => line.trim() !== "");

          if (lines.length > 1) {
            const secondLine = lines[1].split(/\s{2,}/);

            const firstPart = secondLine[0]?.split(/\s+/) || [];
            const details = {
              name: firstPart[0]?.trim() || "정보 없음",
              opSys: firstPart[1]?.trim() || "정보 없음",
            };

            const archStateActivity = secondLine[1]?.split(/\s+/) || [];
            const [arch, state, activity] = archStateActivity;

            const additionalDetails = {
              arch: arch?.trim() || "정보 없음",
              state: state?.trim() || "정보 없음",
              activity: activity?.trim() || "정보 없음",
              loadAverage: secondLine[2]?.trim() || "정보 없음",
              memory: secondLine[3]?.trim() || "정보 없음",
              averageTime: secondLine[4]?.trim() || "정보 없음",
            };

            Object.assign(details, additionalDetails);

            const summary = lines.slice(2).join(" ").split(/\s{2,}/);

            const header = summary[1] + " " + summary[2];

            const x86_64 = [summary[3], summary[4], summary[5], summary[6], summary[7], summary[8], summary[9], summary[10]].join(" ");

            const total = [summary[11], summary[12], summary[13], summary[14], summary[15], summary[16], summary[17], summary[18]].join(" ");

            instance.condorStatus = {
              details,
              header,
              x86_64,
              total
            };
          } else {
            console.error("첫 번째 줄에 데이터가 없습니다.");
          }
        }
      } catch (error) {
        console.error("Condor Status 요청 중 오류가 발생했습니다:", error);
      }
    },
  },
};
</script>

<style scoped>
#app {
  font-family: Arial, sans-serif;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h1 {
  font-size: 2em;
  color: #333;
  margin-bottom: 20px;
}

.loading {
  font-size: 1.5em;
  color: #007bff;
}

.error {
  color: red;
  font-size: 1.2em;
  margin-top: 10px;
}

.select-container {
  margin-top: 20px;
  padding: 10px;
  background-color: #f4f4f4;
  border-radius: 8px;
}

.image-select {
  padding: 8px;
  width: 100%;
  border-radius: 5px;
  border: 1px solid #ccc;
  font-size: 1em;
}

.instances-container {
  margin-top: 40px;
}

.instances-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.instances-table th, .instances-table td {
  padding: 12px;
  border: 1px solid #ddd;
  text-align: center;
}

.instances-table th {
  background-color: #f2f2f2;
}

button {
  margin: 5px;
  padding: 10px 20px;
  cursor: pointer;
  border: none;
  border-radius: 7px;
  transition: background-color 0.3s, transform 0.2s;
}

button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

button:hover:not(:disabled) {
  background-color: #4CAF50;
  color: white;
  transform: scale(1.05);
}

.refresh-btn {
  background-color: #007bff;
  color: white;
  font-size: 0.8em;
  padding: 6px 12px;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
  float: right;
}

.start-btn:hover:not(:disabled) {
  background-color: #45a049;
}

.stop-btn:hover:not(:disabled) {
  background-color: #e53935;
}

.reboot-btn:hover:not(:disabled) {
  background-color: #1976d2;
}

.start-btn {
  background-color: #4CAF50;
}

.stop-btn {
  background-color: #f44336;
}

.reboot-btn {
  background-color: #2196F3;
}

.create-instance-container {
  margin-top: 30px;
}

.create-instance-btn {
  padding: 15px 25px;
  background-color: #007bff;
  color: white;
  font-size: 1.1em;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}

.create-instance-btn:hover {
  background-color: #0056b3;
}

@media (max-width: 768px) {
  #app {
    padding: 10px;
  }

  h1 {
    font-size: 1.5em;
  }

  .image-select {
    font-size: 0.9em;
  }

  button {
    font-size: 0.9em;
    padding: 8px 15px;
  }

  .instances-table th, .instances-table td {
    padding: 8px;
  }

  .create-instance-btn {
    padding: 12px 20px;
  }
}

  .condor-btn {
    background-color: #6c757d;
    color: white;
  }

  .condor-btn:hover {
    background-color: #5a6268;
  }

.condor-status-container {
  margin-top: 10px;
  border: 1px solid #ccc;
  padding: 10px;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.condor-details {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 10px;
}

.condor-details th,
.condor-details td {
  text-align: left;
  padding: 8px;
  border: 1px solid #ddd;
}

.condor-details th {
  background-color: #f2f2f2;
  font-weight: bold;
}

p {
  margin: 0;
  padding: 5px;
  font-size: 14px;
  color: #333;
}

</style>
