<template id="csv-uploader">
    <div>
        <!-- accept only .csv other files can be added -->
        <b-form-file
                v-model="csvFile"
                :state="Boolean(csvFile)"
                accept=".csv"
                placeholder="Choose CSV-File or drop it here.."
                drop-placeholder="Drop CSV-File here.."
        ></b-form-file>
        <p class="mt-3">Selected file: <b>{{ csvFile ? csvFile.name : '' }}</b></p>
        <b-button class="mt-2" @click="csvFile = null">Clear</b-button>
        <b-button class="mt-2" @click="csvSubmit()">Submit</b-button>
    </div>
</template>

<script>
    Vue.component("csv-uploader", {
        template: "#csv-uploader",
        data: function () {
            return {
                csvFile: null,
                fileInput: '',
                fileInputAsJSON: {}
            }
        },
        methods: {
            csvJSON(csv) {
                let lines = csv.split("\n");
                let result = [];
                let headers = lines[0].split(";");

                for (let i = 1; i < lines.length; i++) {
                    let obj = {};
                    let currentLine = lines[i].split(";");
                    for (let j = 0; j < headers.length; j++) {
                        obj[headers[j]] = currentLine[j];
                    }
                    result.push(obj);
                }
                return result;
            },
            csvSubmit() {
                let entities = [];
                const reader = new FileReader();
                let fileInputAsJSON = {};
                reader.onload = ev => {
                    this.fileInput = ev.target.result;
                    fileInputAsJSON = this.csvJSON(this.fileInput);
                    //iterate over fileInputAsJSON and save entities..
                    /*for (let i = 0; i < fileInputAsJSON.length; i++) {
                            if (entities.includes(fileInputAsJSON[i].Target)) entities.push(JSON.parse(fileInputAsJSON[i].Target));
                            if (entities.includes(fileInputAsJSON[i].Source)) entities.push(JSON.parse(fileInputAsJSON[i].Source));
                    }
                     */
                    console.log(entities);
                };
                reader.readAsText(this.csvFile);
            }
        }
    });
</script>