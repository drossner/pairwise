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
                //array with selected items that should be removed
                let selectedComp = this.$refs.adminComparison.selectedCompItems;
                let selectedSpat = this.$refs.adminSpatial.selectedSpatItems;

                //array of objects with all items. remove the items in comp from compItems
                let compItems = this.$refs.adminComparison.items;
                let spatItems = this.$refs.adminSpatial.items;

                //filter for matching item ids and return the list with items without them
                this.$refs.adminComparison.items = compItems.filter(o1 => !selectedComp.some(o2 => o1.id === o2.id));
                this.$refs.adminSpatial.items = spatItems.filter(o1 => !selectedSpat.some(o2 => o1.id === o2.id));

                //send arrays with items that should be removed
                let url = "protected/admin/delete";
                let options = {
                    method: 'POST',
                    body: JSON.stringify({
                        resultComp: selectedComp,
                        resultSpat: selectedSpat
                    })
                };
                fetch(url, options)
                    .then(res => res.json())
                    .then(json => console.log(json))
            }
        }
    });
</script>
