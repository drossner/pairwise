<template id="admin-spatial">
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
                :tbody-tr-class="rowClass"
        >
            <template v-slot:table-busy>
                <div class="text-center my-2">
                    <b-spinner class="align-middle"></b-spinner>
                    <strong>Loading..</strong>
                </div>
            </template>
            <template v-slot:empty>
                <h4 class="text-center">No data available</h4>
            </template>
            <template v-slot:cell(id)="row">
                <b-button block size="sm" @click="row.toggleDetails">{{row.value}}</b-button>
            </template>
            <template v-slot:row-details="row">
                <b-card>
                   <admin-spatial-sub :sessionid="row.item.id"></admin-spatial-sub>
                </b-card>
            </template>
        </b-table>
    </div>
</template>

<script>
    Vue.component("admin-spatial", {
            template: "#admin-spatial",
            data() {
                return {
                    perPage: 20,
                    currentPage: 1,
                    isBusy: true,
                    items: [],
                    fields: [
                        {key: "id", sortable: false},
                        {key: "created", sortable: true},
                        {key: "concept_count", sortable: true},
                        {key: "comp_count", sortable: true},
                        {key: "avg_duration", sortable: true, label: "Average Duration"}
                    ]
                }
            },
            created() {
                fetch("admin/api/getspatialsessions")
                    .then(res => {
                        if (!res.ok) throw Error(res.statusText);
                        else return res;
                    })
                    .then(res => res.json())
                    .then(json => {
                        for (let i = 0; i < json.length; i++) {
                            this.items.push({
                                id: json[i].id,
                                created: json[i].created,
                                concept_count: json[i].conceptCount,
                                comp_count: json[i].compCount,
                                avg_duration: (json[i].avgDuration / 1000) + " sec",
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
                rows() {
                    return this.items.length
                }
            },
            methods: {
                rowClass(item, type) {
                    if (!item) return;
                    if (item.finished === false) return 'table-warning'
                }
            }
        }
    )
</script>
