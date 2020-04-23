<template id="admin-view">
    <div class="row mt-2">
        <div class="col col-12">
            <div>
                <b-button v-if="isAdminFunc()" @click="deleteSessions" class="position-absolute mr-3" variant="primary" style="right: 0">Delete</b-button>
                <p v-else>Nicht authentifiziert</p>
            </div>
            <b-tabs content-class="mt-2" v-model="tabIndex" @input="onTabChanged">
                <b-tab title="Comparison">
                    <admin-comparison ref="adminComparison"></admin-comparison>
                </b-tab>
                <b-tab title="Spatial">
                    <admin-spatial ref="adminSpatial"></admin-spatial>
                </b-tab>
            </b-tabs>
        </div>
    </div>
</template>

<script>
    Vue.component("admin-view", {
        template: "#admin-view",
        data(){
            return {
                tabIndex: 0
            }
        },
        created(){
            if(this.$cookies.isKey("admin-tab-index")){
                this.tabIndex = parseInt(this.$cookies.get("admin-tab-index"), 10);
            } else {
                this.$cookies.set("admin-tab-index", 0)
            }
        },
        methods: {
            onTabChanged(tabIndex){
                this.$cookies.set("admin-tab-index", tabIndex)
            },

            isAdminFunc() {
                let state = this.$javalin.state;
                return state.isAdmin;
            },

            deleteSessions: function() {
                //array with selected item id's that should be removed
                let selectedCompId = this.$refs.adminComparison.selectedCompId;
                let selectedSpatId = this.$refs.adminSpatial.selectedSpatId;

                //array of objects with all items. remove the items in comp from compItems
                let compItems = this.$refs.adminComparison.items;
                let spatItems = this.$refs.adminSpatial.items;

                //filter for matching item ids and return the list with items without them
                this.$refs.adminComparison.items = compItems.filter(o1 => !selectedCompId.some(o2 => o1.id === o2));
                this.$refs.adminSpatial.items = spatItems.filter(o1 => !selectedSpatId.some(o2 => o1.id === o2));

                //send arrays with items that should be removed
                const url = "admin/api/protected/delete";
                console.log(JSON.stringify(selectedCompId));
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
                        if(!res.ok){
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
