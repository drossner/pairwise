<template id="spatial-review">
    <div class="row mt-2">
        <div class="col-12">
            <div id="konvaInspection"></div>
        </div>
    </div>
</template>

<script>
    Vue.component("spatial-review", {
        template: "#spatial-review",
        props: ["sessionid"],
        data(){
            return {
                session: {
                    konvaJson: {}
                }
            }
        },
        created(){
            fetch("admin/api/spatsession/"+this.sessionid)
                .then(res => res.json())
                .then(res => {
                    this.session.konvaJson = res.konvaJson;
                    this.createKonva();
                });
        },
        methods: {
            createKonva(){
                let stage = Konva.Node.create(this.session.konvaJson, 'konvaInspection');
                stage.find(node => true).each(node => node.draggable(false));
            }
        }
    })
</script>
