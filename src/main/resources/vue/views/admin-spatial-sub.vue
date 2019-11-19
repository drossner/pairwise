<template id="admin-spatial-sub">
    <b-table
            responsive hover head-variant="light" show-empty
            selectable select-mode="single" selected-variant="active"
            :items="items" :fields="fields" :busy="isBusy"
            @row-selected="onRowSelected"
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
    </b-table>
</template>

<script>
    Vue.component("admin-spatial-sub", {
        template: "#admin-spatial-sub",
        props: ["sessionid"],
        data(){
            return {
                isBusy: true,
                fields: [
                    {key: "qstNr", sortable: true, label: "Number"},
                    {key: "scale", sortable: true},
                    {key: "duration", sortable: true}
                ],
                items: []
            }
        },
        created(){
            fetch("admin/api/spatsession/"+this.sessionid+"/finishedcomps")
                .then(res => res.json())
                .then(json => {
                    for(let i = 0; i < json.length; i++){
                        this.items.push({
                            qstNr: json[i].number,
                            scale: json[i].scale,
                            duration: json[i].duration
                        });
                        this.isBusy = false;
                    }
                })
        },
        methods: {
            onRowSelected(item) {
                if (item === null) return; //pagination
                location.href = "admin/spat/" + this.sessionid + "?qstNr="+item[0].qstNr;
            }
        }
    })
</script>