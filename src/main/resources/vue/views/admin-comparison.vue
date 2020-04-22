<template id="admin-comparison">
    <div>
        <b-pagination
                v-model="currentPage"
                :total-rows="rows"
                :per-page="perPage"
        ></b-pagination>
        <b-table
                responsive striped hover head-variant="light" show-empty foot-clone
                selectable select-mode="single" selected-variant="active"
                :items="items" :fields="fields" :busy="isBusy"
                :per-page="perPage" :current-page="currentPage"
                @row-selected="onRowSelected" :tbody-tr-class="rowClass"
        >
            <template v-slot:cell(delete)="item" v-if="isAdminFunc()">
                <b-form-checkbox type="checkbox" :value="item.item" v-model="selectedCompItems"></b-form-checkbox>
            </template>

            <template v-slot:table-busy>
                <div class="text-center my-2">
                    <b-spinner class="align-middle"></b-spinner>
                    <strong>Loading..</strong>
                </div>
            </template>
            <template v-slot:empty>
                <h4 class="text-center">No data available</h4>
            </template>
        </b-table>
    </div>
</template>

<script>
    Vue.component("admin-comparison", {
        template: "#admin-comparison",
        data(){
            return {
                perPage: 20,
                currentPage: 1,
                isBusy: true,
                selectedCompItems: [],
                items: [],
                fields:[
                    {key: "id", sortable: false},
                    {key: "created", sortable: true},
                    {key: "comparison_count", sortable: true},
                    {key: "avg_rating", sortable: true},
                    {key: "avg_duration", sortable: true},
                    {key: "delete", label: "Delete", sortable: false}
                ]
            }
        },
        created(){
            fetch("admin/api/getcompsessions")
                .then(res => {
                    if(!res.ok) throw Error(res.statusText);
                    else return res;
                })
                .then(res => res.json())
                .then(json => {
                    for(let i = 0; i < json.length; i++){
                        this.items.push({
                            id: json[i].id,
                            created: json[i].created,
                            comparison_count: json[i].compCount,
                            avg_rating: json[i].avgRating,
                            avg_duration: (json[i].avgDuration / 1000)+" sec",
                            finished: json[i].finished
                        });
                    }
                    this.isBusy = false;
                })
                .catch(err => {
                    $.toast("Server Error");
                    this.isBusy = false
                })
        },
        computed: {
            rows(){return this.items.length}
        },
        methods: {
            onRowSelected(item){
                if(item === null) return; //pagination
                location.href = "admin/comp/"+item[0].id;
            },
            rowClass(item, type){
                if(!item) return;
                if(item.finished === false) return 'table-warning'
            },
            isAdminFunc() {
                let state = this.$javalin.state;
                return state.isAdmin;
            }
        }
    })
</script>
