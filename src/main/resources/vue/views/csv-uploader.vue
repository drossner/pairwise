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
                sample: [],
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
                this.sample = [];
                this.rand = [];
                this.avgWeight = 0;
                this.infoFlag = true;
            },

            csvJSON(csv) {
                let lines = csv.split("\n");
                let result = [];
                let headers = lines[0].split(";");
                //headers = headers.join('~').toLowerCase().split('~');
                for (let i = 0; i < headers.length; i++) headers[i] = headers[i].toLowerCase();

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
                        if (!this.entities.includes(this.fileInputAsJSON[i].target))
                            this.entities.push(this.fileInputAsJSON[i].target);
                        if (!this.entities.includes(this.fileInputAsJSON[i].source))
                            this.entities.push(this.fileInputAsJSON[i].source);
                    }
                    //filters NaN, null, not defined and ''
                    this.entities = this.entities.filter(el => {
                        return el
                    });
                    //random sample
                    this.getSample(this.entities, 30);

                    for (let i = 0; i < this.fileInputAsJSON.length; i++) {
                        if (this.rand.includes(this.fileInputAsJSON[i].target) && this.rand.includes(this.fileInputAsJSON[i].source)) {
                            this.sample.push(this.fileInputAsJSON[i]);
                            ctx++;
                        }
                    }

                    //calculate the average weight of the CSV file
                    let sum = 0;
                    for (let i = 0; i < this.fileInputAsJSON.length; i++) {
                        sum = sum + parseFloat(this.fileInputAsJSON[i].weight);
                    }
                    this.avgWeight = sum/this.fileInputAsJSON.length;
                };
                reader.readAsText(this.csvFile);
                ctx = 0;
                this.infoFlag = false;
                console.log(this.sample);
            },

            //array mit zahlen 0 bis indicies, zufällig rausholen und aus array löschen.
            getSample(array, count) {
                for (let i = 0; i < count; i++)
                    this.rand.push(array[Math.floor(Math.random() * array.length)])
            },

            csvSubmit() {
                let urlConnections = "api/fillConnections";
                let optionsConnections = {
                    method: 'POST',
                    header: {'Content-Type': 'application/json'},
                    body: JSON.stringify(this.sample)
                };

                fetch(urlConnections, optionsConnections)
                    //.then(res => res.json())
                    .then(json => console.log('Success: ', json))
                    .catch(error => console.error('Error: ', error));

                /*let urlConcept = "api/fillConcept";
                let optionsConcept = {
                    method: 'POST',
                    header: {'Content-Type': 'application/json'},
                    body: JSON.stringify({entities: this.rand})
                };
                fetch(urlConcept, optionsConcept)
                    //.then(res => res.json())
                    .then(json => console.log('Success: ', json))
                    .catch(error => console.error('Error: ', error));

                 */
                this.infoFlag = true;
            }
        }
    });
</script>