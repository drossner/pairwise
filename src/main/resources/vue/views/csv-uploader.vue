<template id="csv-uploader">
    <div>
        <b-popover target="CompText" triggers="hover" placement="topleft">
            <template v-slot:title>Pair Comparison Test</template>
            <p>Rate the relationship between two concepts from your sample. You
                have to rate the relationship between one and ten, with one being the lowest and ten the highest.</p>
            <p>The value indicates how many comparisons you want to have per test. Based on our research, a user needs
                about 40 seconds for a test with eight comparisons.</p>
        </b-popover>
        <b-popover target="SpatText" triggers="hover" placement="topleft">
            <template v-slot:title>Spatial Comparison Test</template>
            <p>comparison of several concepts in space. You can position the terms with drag and drop. The further the
                nodes are, the less they have to do with each other.</p>
            <p>You can specify the number of tests and how many terms they contain.Based on our research, we recommend
                six tests with five concepts each.</p>
        </b-popover>

        <!-- accept only .csv other files can be added -->
        <b-row class="mt-3 text-center" align-h="center">
            <b-col cols="6"><h1><b>File Uploader</b></h1></b-col>
        </b-row>

        <b-row class="mt-3" align-h="center">
            <b-col cols="6">
                <p>Please select a CSV-file with which you would like to start a survey.</p>
                <b-form-file
                        ref="csv-input"
                        v-model="csvFile"
                        :state="Boolean(csvFile)"
                        accept=".csv"
                        @input="csvInfo"
                        placeholder="Choose CSV-File or drop it here.."
                        drop-placeholder="Drop CSV-File here.."
                ></b-form-file>
            </b-col>
        </b-row>

        <b-row align-h="center">
            <b-col cols="3">
                <div class="mt-3 text-center">
                    <label>Entities: {{ entities ? entities.length : 0 }}</label>
                </div>
            </b-col>
            <b-col cols="3">
                <div class="mt-3 text-center">
                    <label>Average weight: {{ avgWeight ? avgWeight : 0 }}</label>
                </div>
            </b-col>
        </b-row>

        <b-row class="mt-3" align-h="center">
            <b-col class="text-center" cols="3"><label>Sample quantity:</label></b-col>
            <b-col cols="3">
                <b-form-input :disabled="!Boolean(csvFile)"
                              aria-describedby="quantity-feedback"
                              align-h="left"
                              type="number"
                              v-model="quantity"
                              :state="quantityState"
                              :min="2"
                              step="1">
                </b-form-input>
                <b-form-invalid-feedback id="quantity-feedback">Please choose a file first. Number must be at least 2 or at most the size of the entities.</b-form-invalid-feedback>
            </b-col>
        </b-row>

        <!-- choose the tests and customize them -->
        <b-row class="mt-3" align-h="center">
            <b-col class="text-center" cols="6"><h3><b>Choose your Tests</b></h3></b-col>
        </b-row>
        <b-row class="mt-3" align-h="center">
            <b-col id="CompText" class="text-center" cols="2"><label><b>Pair Comparison Test</b></label></b-col>
            <b-col class="text-center" cols="1">
                <b-form-checkbox
                        id="CompCheckbox"
                        v-model="status_comp"
                        value="comp_accepted"
                        unchecked-value="comp_not_accepted"></b-form-checkbox>
            </b-col>
            <b-col id="SpatText" class="text-center" cols="2"><label><b>Spatial Comparison Test</b></label></b-col>
            <b-col class="text-center" cols="1">
                <b-form-checkbox
                        id="SpatCheckbox"
                        v-model="status_spat"
                        value="spat_accepted"
                        unchecked-value="spat_not_accepted"></b-form-checkbox>
            </b-col>
        </b-row>
        <b-row align-h="center">
            <b-col cols="3">
                <div class="mt-3">
                    <p>Number of pair comparisons:</p>
                    <b-form-input :disabled="status_comp === 'comp_not_accepted'"
                                  align-h="left"
                                  required
                                  type="number"
                                  :state="numberOfCompsState"
                                  v-model="number_of_comparisons"
                                  aria-describedby="number-of-pair-feedback"
                                  :min="1"
                                  step="1">
                    </b-form-input>
                    <b-form-invalid-feedback id="number-of-pair-feedback">Number must be greater than 1 and not more than the half
                        quantity.
                    </b-form-invalid-feedback>
                </div>
            </b-col>
            <b-col cols="3">
                <div>
                    <p class="mt-3">Number of spatial tests:</p>
                    <b-form-input class="mt-3"
                                  :disabled="status_spat === 'spat_not_accepted'"
                                  align-h="left"
                                  type="number"
                                  :state="numberOfTestsState"
                                  v-model="number_of_tests"
                                  aria-describedby="number-of-spat-feedback"
                                  :min="1"
                                  step="1">
                    </b-form-input>
                    <p class="mt-3">Number of nodes per test:</p>
                    <b-form-input class="mt-3"
                                  :disabled="status_spat === 'spat_not_accepted'"
                                  align-h="left"
                                  type="number"
                                  :state="numberOfTestsState"
                                  v-model="nodes_per_test"
                                  aria-describedby="number-of-spat-feedback"
                                  :min="2"
                                  step="1">
                    </b-form-input>
                    <b-form-invalid-feedback id="number-of-spat-feedback">Applied to spatial tests and their nodes: number of tests
                        times nodes per test must be less than or equal to quantity. tests*nodes <= quantity
                    </b-form-invalid-feedback>
                </div>
            </b-col>
        </b-row>

        <b-row class="mt-3" align-h="center">
            <b-col cols="3">
                <b-button block @click="clearFiles()" variant="primary">Clear</b-button>
            </b-col>
            <b-col cols="3">
                <b-button :disabled="((status_comp === 'comp_not_accepted') && (status_spat === 'spat_not_accepted')) || (numberOfCompsState === false) || (quantityState === false) || (numberOfTestsState === false)" block @click="csvSubmit()" variant="primary">Submit</b-button>
            </b-col>
        </b-row>
    </div>
</template>

<script>
    Vue.component("csv-uploader", {
        template: "#csv-uploader",
        data: function () {
            return {
                //upload
                csvFile: null,
                fileInput: '',
                fileInputAsJSON: {},
                entities: [],
                sample: [],
                rand: [],
                avgWeight: 0,
                limit: 0,

                //test customization
                quantity: 2,
                status_comp: 'comp_not_accepted',
                status_spat: 'spat_not_accepted',
                number_of_comparisons: 1,
                number_of_tests: 1,
                nodes_per_test: 2

            }
        },

        computed: {
            numberOfCompsState() {
                return ((this.number_of_comparisons * 2 <= this.quantity) && (this.number_of_comparisons >= 1))
            },
            quantityState() {
                return this.quantity >= 2 && this.quantity <= this.entities.length
            },
            numberOfTestsState() {
                return this.number_of_tests * this.nodes_per_test <= this.quantity
            }
        },

        methods: {
            clearFiles() {
                this.$refs["csv-input"].reset();
                this.quantity = 0;
                this.csvFile = null;
                this.fileInput = '';
                this.fileInputAsJSON = {};
                this.entities = [];
                this.sample = [];
                this.rand = [];
                this.avgWeight = 0;

                this.quantity = 2;
                this.status_comp = 'comp_not_accepted';
                this.status_spat = 'spat_not_accepted';
                this.number_of_comparisons = 1;
                this.number_of_tests = 1;
                this.nodes_per_test = 2;
            },

            /**
             * converts an csv to an array with JSON objects
             * @param csv with the connections between source target and their sum and weight
             * @returns {Array} with JSON objects
             */
            csvJSON(csv) {
                let lines = csv.split("\n");
                let result = [];
                let headers = lines[0].split(";");
                //headers = headers.join('~').toLowerCase().split('~');
                for (let i = 0; i < headers.length; i++) headers[i] = headers[i].toLowerCase();

                for (let i = 1; i < lines.length - 1; i++) {
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
                if(this.csvFile) {
                    const reader = new FileReader();
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

                        //calculate the average weight of the CSV file
                        let sum = 0;
                        for (let i = 0; i < this.fileInputAsJSON.length; i++) {
                            sum = sum + parseFloat(this.fileInputAsJSON[i].weight);
                        }
                        this.avgWeight = sum / this.fileInputAsJSON.length;
                    };
                    reader.readAsText(this.csvFile);
                }
            },

            //
            getSample() {
                let idx, element;
                for (let i = 0, limit = this.entities.length; i < this.quantity; i++) {
                    //console.log(limit);
                    //Math.floor return the biggest integer which is bigger or equals the given number (round off!)
                    //random ziehen. nummer wird in idx gespeichert und das random element in element
                    //save the random index in idx, and the random element in element
                    idx = Math.floor((Math.random() * limit));
                    element = this.entities[idx];
                    //count limit one down, because we have drawn one element
                    limit--;
                    //save the lase element from entities where the random element was and save the random element
                    //where the last element was. due to limit-- the last element wont be reached.
                    this.entities[idx] = this.entities[limit];
                    this.entities[limit] = element;
                    this.rand.push(element);
                }
            },

            //csvSubmit as asynchronous fetch connections wait for the concepts
            async csvSubmit() {

                let ctx = 0;
                this.getSample();

                //filter the matching random entities with the connections from the csv
                for (let i = 0; i < this.fileInputAsJSON.length; i++) {
                    if (this.rand.includes(this.fileInputAsJSON[i].target) && this.rand.includes(this.fileInputAsJSON[i].source)) {
                        this.sample.push(this.fileInputAsJSON[i]);
                        ctx++;
                    }
                }
                ctx = 0;

                //send the entities to the server
                let urlConcept = "api/fillconcept";
                let optionsConcept = {
                    method: 'POST',
                    header: {'Content-Type': 'application/json'},
                    body: JSON.stringify({entities: this.rand})
                };
                await fetch(urlConcept, optionsConcept)
                    .then(json => console.log('Success: ', json))
                    .catch(error => console.error('Error: ', error));

                //send the connections to the server
                let urlConnections = "api/fillconnections";
                let optionsConnections = {
                    method: 'POST',
                    header: {'Content-Type': 'application/json'},
                    body: JSON.stringify(this.sample)
                };
                await fetch(urlConnections, optionsConnections)
                    .then(json => console.log('Success: ', json))
                    .catch(error => console.error('Error: ', error));


                let body = {};
                if(this.status_spat === 'spat_not_accepted'){
                    body = JSON.stringify({
                        maxComps : this.number_of_comparisons,
                        conceptsPerSpat:  0,
                        maxSpats: 0,
                        status_spat: this.status_spat,
                        status_comp: this.status_comp
                    })
                }
                else if(this.status_comp === 'comp_not_accepted'){
                    body = JSON.stringify({
                        maxComps : 0,
                        conceptsPerSpat:  this.nodes_per_test,
                        maxSpats: this.number_of_tests,
                        status_spat: this.status_spat,
                        status_comp: this.status_comp
                    })
                }
                else {
                    body = JSON.stringify({
                        maxComps : this.number_of_comparisons,
                        conceptsPerSpat:  this.nodes_per_test,
                        maxSpats: this.number_of_tests,
                        statusSpat: this.status_spat,
                        statusComp: this.status_comp
                    })
                }

                //send the customized settings
                let urlSettings = "api/fillsettings";
                let optionsSettings = {
                    method: 'POST',
                    header: {'Content-Type': 'application/json'},
                    body: body
                };
                fetch(urlSettings, optionsSettings)
                    .then(json => console.log('Success: ', json))
                    .catch(error => console.error('Error: ', error));

                this.clearFiles();
            }
        }
    });
</script>