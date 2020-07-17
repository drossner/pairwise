<template id="spatial-review-replay">
    
</template>

<script>
    //TODO: code for replay
    Vue.component("spatial-review-replay", {
        template: "spatial-review-replay",
        data() {
            return {
                session: {
                    konvaJson: {},
                    duration: 0
                },
                nodes: {
                    x: 0,
                    y: 0,
                    oldX: 0,
                    oldY: 0,
                    dragStart: 0,
                    dragStop: 0
                }
            }
        },
        created() {
            fetch("admin/api/spatsession/"+this.sessionid+"?qstNr="+this.qstnr)
                .then(res => res.json())
                .then(res => {
                    this.session = res;
                    this.createKonva()
                });

            fetch("/admin/api/replay")
                .then(res => res.json())
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
