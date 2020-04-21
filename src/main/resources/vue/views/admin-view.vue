<template id="admin-view">
    <div class="row mt-2">
        <div class="col col-12">
            <div>
                <b-button v-if="isAdminFunc()" class="position-absolute mr-3" variant="primary" style="right: 0">Delete</b-button>
                <p v-else>Nicht authentifiziert</p>
            </div>
            <b-tabs content-class="mt-2" v-model="tabIndex" @input="onTabChanged">
                <b-tab title="Comparison">
                    <admin-comparison></admin-comparison>
                </b-tab>
                <b-tab title="Spatial">
                    <admin-spatial></admin-spatial>
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
            }
        }
    });
</script>
