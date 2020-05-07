<template id="csv-uploader">
    <div>
        <!-- accept only .csv other files can be added -->
        <b-form-file
                ref="csv-input"
                v-model="csvFile"
                :state="Boolean(csvFile)"
                accept=".csv"
                placeholder="Choose CSV-File or drop it here.."
                drop-placeholder="Drop CSV-File here.."
        ></b-form-file>
        <!-- <p class="mt-3">Selected file: <b>{{ csvFile ? csvFile.name : '' }}</b></p> -->
        <div>
            <label>Entities: <b>{{ entities ? entities.length : 0 }}</b></label>
        </div>
        <div>
            <label>Average weight: <b>{{ avgWeight ? avgWeight : 0 }}</b></label>
        </div>
        <b-button class="mt-2" @click="clearFiles()">Clear</b-button>
        <b-button v-if="infoFlag" class="mt-2" @click="csvInfo()">Info</b-button>
        <b-button v-else class="mt-2" @click="csvSubmit()">Submit</b-button>
    </div>
</template>

<script>
    Vue.component("csv-uploader", {
        template: "#csv-uploader",
        data: function () {
            return {
                csvFile: null,
                fileInput: '',
                fileInputAsJSON: {},
                entities: [],
                sample: {},
                rand: [],
                avgWeight: 0,
                infoFlag: true
            }
        },
        methods: {
            clearFiles() {
                this.$refs["csv-input"].reset();
                this.csvFile = null;
                this.fileInput= '';
                this.fileInputAsJSON = {};
                this.entities = [];
                this.sample = {};
                this.rand = [];
                this.avgWeight = 0;
                this.infoFlag = true;
            },

            csvJSON(csv) {
                let lines = csv.split("\n");
                let result = [];
                let headers = lines[0].split(";");

                for (let i = 1; i < lines.length-1; i++) {
                    let obj = {};
                    let currentLine = lines[i].split(";");
                    for (let j = 0; j < headers.length; j++) {
                        obj[headers[j]] = currentLine[j];
                    }
                    result.push(obj);
                }
                return result;
            },

            csvInfo() {
                const reader = new FileReader();
                let ctx = 0;
                reader.onload = ev => {
                    this.fileInput = ev.target.result;
                    this.fileInputAsJSON = this.csvJSON(this.fileInput);

                    //iterate over fileInputAsJSON and save entities..
                    for (let i = 0; i < this.fileInputAsJSON.length; i++) {
                        if (!this.entities.includes(this.fileInputAsJSON[i].Target)) this.entities.push(this.fileInputAsJSON[i].Target);
                        if (!this.entities.includes(this.fileInputAsJSON[i].Source)) this.entities.push(this.fileInputAsJSON[i].Source);
                    }
                    //filters NaN, null, not defined and ''
                    this.entities = this.entities.filter(el => {
                        return el
                    });
                    //random sample
                    this.getSample(this.entities, 30);

                    for (let i = 0; i < this.fileInputAsJSON.length; i++) {
                        if (this.rand.includes(this.fileInputAsJSON[i].Target) && this.rand.includes(this.fileInputAsJSON[i].Source)) {
                            this.sample[ctx] = this.fileInputAsJSON[i];
                            ctx++;
                        }
                    }

                    //calculate the average weight of the CSV file
                    let sum = 0;
                    for (let i = 0; i < this.fileInputAsJSON.length; i++) {
                        sum = sum + parseFloat(this.fileInputAsJSON[i].Weight);
                    }
                    this.avgWeight = sum/this.fileInputAsJSON.length;
                };
                reader.readAsText(this.csvFile);
                ctx = 0;
                this.infoFlag = false;
            },

            getSample(array, count) {
                for (let i = 0; i < count; i++)
                    this.rand.push(array[Math.floor(Math.random() * array.length)])
            },

            csvSubmit() {

            }
        }
    });
</script>