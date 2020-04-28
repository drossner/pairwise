<template id="admin-view">
    <div class="row mt-2">
        <div class="col col-12">
            <p v-if="!isAdminFunc()">Nicht authentifiziert</p>
            <b-button-group class="position-absolute mr-3" style="right: 0">
                <b-button v-if="isAdminFunc()" @click="deleteSessions" variant="outline-primary">Delete</b-button>
                <b-button @click="hideUncompleted" :pressed.sync="toggle" value="btnText" variant="outline-primary">{{ btnText }}
                </b-button>
            </b-button-group>
            <b-tabs content-class="mt-2" v-model="tabIndex" @input="onTabChanged">
                <b-tab title="Comparison">
                    <admin-comparison v-on:comp-loaded="loadCompletedComp($event)" ref="adminComparison"></admin-comparison>
                </b-tab>
                <b-tab title="Spatial">
                    <admin-spatial v-on:spat-loaded="loadCompletedSpat($event)" ref="adminSpatial"></admin-spatial>
                </b-tab>
            </b-tabs>
        </div>
    </div>
</template>

<script>
    Vue.component("admin-view", {
        template: "#admin-view",
        data() {
            return {
                tabIndex: 0,
                toggle: false,
                btnText: '',
                compItems: [],
                spatItems: [],
                completedCompSessions: [],
                completedSpatSessions: []
            }
        },
        created() {
            if (this.$cookies.isKey("admin-tab-index")) {
                this.tabIndex = parseInt(this.$cookies.get("admin-tab-index"), 10);
            } else {
                this.$cookies.set("admin-tab-index", 0)
            }

            if (this.$cookies.isKey("hide-uncompleted")) {
                if (JSON.parse(this.$cookies.get("hide-uncompleted"))) {
                    this.btnText = "Show";
                }
                else this.btnText = "Hide"
            }
            else {
                this.btnText = "Hide";
            }
        },

        methods: {
            loadCompletedSpat(event) {
                this.spatItems = this.$refs.adminSpatial.items;
                for (let i = 0; i < event.length; i++)
                    this.completedSpatSessions[i] = event[i];

                if (this.$cookies.isKey("hide-uncompleted")) {
                    this.toggle = JSON.parse(this.$cookies.get("hide-uncompleted"));
                    //this.btnText = "Show";
                    if (JSON.parse(this.$cookies.get("hide-uncompleted"))) {
                        this.$refs.adminSpatial.items = this.completedSpatSessions;
                    }
                }
                else {
                    this.$cookies.set("hide-uncompleted", this.toggle);
                    //this.btnText = "Hide";
                }
            },

            loadCompletedComp(event) {
                this.compItems = this.$refs.adminComparison.items;
                for(let i = 0; i < event.length; i++)
                    this.completedCompSessions[i] = event[i];

                if(this.$cookies.isKey("hide-uncompleted")) {
                    this.toggle = JSON.parse(this.$cookies.get("hide-uncompleted"));
                    if(JSON.parse(this.$cookies.get("hide-uncompleted"))) {
                        this.$refs.adminComparison.items = this.completedCompSessions;
                        //this.btnText = "Show";
                    }
                } else {
                    this.$cookies.set("hide-uncompleted", this.toggle);
                    //this.btnText = "Hide";
                }
            },

            onTabChanged(tabIndex) {
                this.$cookies.set("admin-tab-index", tabIndex)
            },

            isAdminFunc() {
                let state = this.$javalin.state;
                return state.isAdmin;
            },

            hideUncompleted() {
                if (!JSON.parse(this.$cookies.get("hide-uncompleted"))) {
                    this.$refs.adminComparison.items = this.completedCompSessions;
                    this.$refs.adminSpatial.items = this.completedSpatSessions;
                    this.$cookies.set("hide-uncompleted", this.toggle);
                    this.btnText = "Show";

                } else {
                    this.$refs.adminComparison.items = this.compItems;
                    this.$refs.adminSpatial.items = this.spatItems;
                    this.$cookies.set("hide-uncompleted", this.toggle);
                    this.btnText = "Hide";
                }
            },
            deleteSessions() {
                //array with selected item id's that should be removed
                let selectedCompId = this.$refs.adminComparison.selectedCompId;
                let selectedSpatId = this.$refs.adminSpatial.selectedSpatId;

                //filter for matching item ids and return the list with items without them
                this.$refs.adminComparison.items = this.compItems.filter(o1 => !selectedCompId.some(o2 => o1.id === o2));
                this.$refs.adminSpatial.items = this.spatItems.filter(o1 => !selectedSpatId.some(o2 => o1.id === o2));
                this.compItems = this.compItems.filter(o1 => !selectedCompId.some(o2 => o1.id === o2));
                this.spatItems = this.spatItems.filter(o1 => !selectedSpatId.some(o2 => o1.id === o2));

                //send arrays with items that should be removed
                const url = "admin/api/protected/delete";
                let options = {
                    method: 'POST',
                    header: {'Content-Type': 'application/json'},
                    body: JSON.stringify({
                        resultComp: selectedCompId,
                        resultSpat: selectedSpatId
                    })
                };
                fetch(url, options)
                    .then(res => {
                        if (!res.ok) {
                            this.$refs.adminComparison.selectedCompId = [];
                            this.$refs.adminSpatial.selectedSpatId = [];
                            throw new Error("HTTP error " + res.status);
                        }
                        this.$refs.adminComparison.selectedCompId = [];
                        this.$refs.adminSpatial.selectedSpatId = [];
                        return res;
                    })
            }
        }
    });
</script>
