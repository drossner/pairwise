<template id="admin-view">
    <div class="row mt-2">
        <div class="col col-12">
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
            }
        }
    });
</script>
