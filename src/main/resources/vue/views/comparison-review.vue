<template id="comparison-review">
    <div class="row mt-2">
        <div class="col col-12">
            <b-table
                    responsive striped hover head-variant="light" show-empty :busy="isBusy"
                    :items="items" :fields="fields">
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

    </div>
</template>

<script>
    Vue.component("comparison-review", {
        template: "#comparison-review",
        props: ["sessionid"],
        data(){
            return{
                isBusy: true,
                fields: [
                    {key: "concept_a", sortable: true},
                    {key: "rating", sortable: true},
                    {key: "duration", sortable: true},
                    {key: "concept_b", sortable: true}
                ],
                items: []
            }
        },
        created(){
            fetch("admin/api/compsession/"+this.sessionid)
                .then(res => res.json())
                .then(res => {
                    json = res.comparisons;
                    for(let i = 0; i < json.length; i++){
                        this.items.push({
                            concept_a: json[i].conceptA,
                            concept_b: json[i].conceptB,
                            rating:    json[i].rating,
                            duration:  json[i].duration + "ms"
                        })
                    }
                    this.isBusy = false;
                });
        }
    })
</script>
