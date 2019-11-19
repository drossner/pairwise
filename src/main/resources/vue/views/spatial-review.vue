<template id="spatial-review">
    <div>
    <div class="row mt-2 position-relative">
        <div class="col col-12" id="konvaInspection"></div>
        <b-button-group style="left: 0; bottom: 0" class="position-absolute m-2 ml-4" variant="primary">
            <b-button :disabled="btnBar.left.disabled" :href="btnBar.left.href">&laquo;</b-button>
            <b-button :disabled="btnBar.right.disabled" :href="btnBar.right.href">&raquo;</b-button>
        </b-button-group>
    </div>
    <div class="row">
        <div class="col col-12">
            <p><strong>Duration</strong> {{session.duration/1000}} seconds</p>
        </div>
    </div>
        </div>
</template>

<script>
    Vue.component("spatial-review", {
        template: "#spatial-review",
        props: ["sessionid", "qstnr", "maxnr"],
        data(){
            return {
                btnBar: {
                    left: {disabled: true, href: "#"},
                    right: {disabled: true, href: "#"}
                },
                stage: {},
                session: {
                    konvaJson: {},
                    duration: 0,
                    clicksPerConcept: []
                }
            }
        },
        created(){
            fetch("admin/api/spatsession/"+this.sessionid+"?qstNr="+this.qstnr)
                .then(res => res.json())
                .then(res => {
                    //this.session.konvaJson = res.konvaJson;
                    //this.session.duration = res.duration;
                    this.session = res;
                    this.createKonva();
                });
            if(this.qstnr < this.maxnr){
                this.btnBar.right.disabled = false;
                let searchParams = new URLSearchParams(location.search);
                searchParams.set("qstNr", 1+parseInt(this.qstnr));
                this.btnBar.right.href = location.pathname+"?"+searchParams.toString();
            }
            if(this.qstnr > 0){
                this.btnBar.left.disabled = false;
                let searchParams = new URLSearchParams(location.search);
                searchParams.set("qstNr", -1+parseInt(this.qstnr));
                this.btnBar.left.href = location.pathname+"?"+searchParams.toString();
            }
        },
        methods: {
            createKonva(){
                this.stage = Konva.Node.create(this.session.konvaJson, 'konvaInspection');
                //stage.find(node => true).each(node => node.draggable(false));
                let groupArr = this.stage.find('Group').toArray();
                for(let i = 0; i < groupArr.length; i++){
                    groupArr[i].draggable(false);
                    let temp = this.session.clicksPerConcept[i];
                    let clickText = new Konva.Text({
                        x: 0, y: 50,
                        width: 100, height: 80, //groupsize todo: magic value..
                        text: '('+temp+')',
                        padding: 10,
                        fontSize: 15,
                        align: 'center',
                        fill: 'red'
                    });
                    groupArr[i].add(clickText);
                }
                //this.stage.scaleX(1); this.stage.scaleY(1);
                this.stage.batchDraw();
            }
        }
    })
</script>
