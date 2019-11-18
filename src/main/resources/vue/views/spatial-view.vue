<template id="spatial-view">
    <div class="row spatial-row mt-2 position-relative">
        <div class="col col-12" id="canvasContainer" ref="canvasContainer"></div>
        <b-button @click="sendToServer" style="right: 0" class="position-absolute m-2 " variant="primary">Next</b-button>
    </div>
</template>

<script>
    Vue.component("spatial-view", {
        template: '#spatial-view',
        data: function(){
            return {
                stage: null,
                concepts: [],
                timeStamp: 0,
                clicksPerConcept: []
            }
        },
        mounted(){
            //this.createPollCanvas(this.$refs.canvasContainer);
            this.resize();
        },
        created(){
            window.addEventListener("resize", this.resize);
            fetch("api/spatial/concepts")
                .then(res => res.json())
                .then(json => {
                    this.concepts = json;
                    for(let i = 0; i < this.concepts.length; i++){ this.clicksPerConcept.push(0) }
                    this.createPollCanvas(this.$refs.canvasContainer);
                    this.resize();
                    this.stage.draw();
                    this.timeStamp = performance.now();
                });
        },
        methods: {
            resize: function(){
                const container = this.$refs.canvasContainer;
                if(!container) return;
                if(this.stage === null) return;
                const h = container.offsetHeight;
                const w = container.offsetWidth;
                this.stage.width(w);
                this.stage.height(h);
                if(w < 576 ) this.stage.scale({x: 0.55, y: 0.55});
                else if(w >= 576 && w < 768) this.stage.scale({x: 0.7, y: 0.7});
                else if(w >= 768 && w < 992) this.stage.scale({x: 0.9, y: 0.9});
                else if(w >= 992) this.stage.scale({x: 1, y: 1});
                //$.toast('Width: '+w);
                this.stage.batchDraw();
                //console.log(this.stage.scale());
            },
            createPollCanvas: function (containerID) {
                const stage = new Konva.Stage({
                    container: containerID,   // id of container <div>
                    width: 800,
                    height: 800
                });
                this.stage = stage;
                const mainLayer = new Konva.Layer();
                const lineLayer = new Konva.Layer();
                let concepts = this.concepts.map(concept => concept.name);
                let nodes = [5];
                let self = this;
                for(let i = 0; i < concepts.length; i++){
                    nodes[i] = this.createNode(concepts[i], 40+i*4, 40+i*3);
                    nodes[i].on('mousedown touchstart', function(){
                        self.clicksPerConcept[i]++;
                    });
                    this.concepts[i].konvaNode = nodes[i];
                }

                let lines = this.createLines(nodes);

                nodes.forEach(value => {
                    mainLayer.add(value);
                });

                lines.forEach(value => {
                    lineLayer.add(value);
                });

                stage.add(lineLayer);
                stage.add(mainLayer);
            },

            createLines: function (nodeArr) {
                let lineArr = [];
                for(let i = 0; i < nodeArr.length - 1; i++){
                    for(let k = i + 1; k < nodeArr.length; k++){
                        //line between i and k
                        let nk = nodeArr[k], ni = nodeArr[i];
                        let iBox = this.getGroupBox(ni);
                        let kBox = this.getGroupBox(nk);
                        let line = new Konva.Line({
                            points: [
                                nk.x()+kBox.width/2, nk.y()+kBox.height/2,
                                ni.x()+iBox.width/2, ni.y()+iBox.height/2], //x,y , x,y , ... take the middle
                            stroke: 'lightgray',
                            strokeWidth: 2,
                        });
                        let self = this;
                        nk.on('dragmove', function(){
                            self.updateLine.apply(self, [nk, ni, line]);
                        });
                        ni.on('dragmove', function(){
                            self.updateLine.apply(self, [nk, ni, line]);
                        });
                        lineArr.push(line);
                    }
                }
                return lineArr;
            },
            createNode: function (text, x, y) {
                const width = 100, height = 80;
                const group = new Konva.Group({
                    x: x, y: y,
                    draggable: true
                });

                let rect = new Konva.Rect({
                    x: 0, y: 0,
                    width: width, height: height,
                    stroke: 'black', strokeWidth: 1, fill: 'white'
                });

                let content = new Konva.Text({
                    x: 0, y: 0,
                    width: width, height: height,
                    text: text,
                    padding: 10,
                    fontSize: 20,
                    align: 'center'
                });

                group.add(rect);
                group.add(content);
                return group;
            },
            updateLine: function (nodeA, nodeB, line) {
                let aBox = this.getGroupBox(nodeA);
                let bBox = this.getGroupBox(nodeB);
                line.points(
                    [
                        nodeA.x()+aBox.width/2, nodeA.y()+aBox.height/2,
                        nodeB.x()+bBox.width/2, nodeB.y()+bBox.height/2]
                );
                let fstA = line.points().slice(0, 2);
                let sndA = line.points().slice(2, 4);
                let length = Math.sqrt(Math.pow(fstA[0]-sndA[0], 2) + Math.pow(fstA[1]-sndA[1], 2));
                //test
                //fix distance by angle..
                //end test
                let minStroke = 0.1;
                let maxStroke = 80;
                //~
                //1000 => 0.1
                //60 => 80
                let normedLength = Math.min(length, 1000);
                normedLength = Math.max(normedLength, 60);
                let normedStroke = (-0.03993403403) * normedLength + 40; //(2162/27); //Mathemagie (leicht verzaubert für 60..
                let stroke = Math.min((1/length)*1500, 70);
                line.strokeWidth(normedStroke/2); //1118 vertical px in 1000 * 500
                line.getLayer().batchDraw();
            },
            getGroupBox: function (group) {
                return group.getChildren()[0].size();
            },
            sendToServer: function () {
                const container = this.$refs.canvasContainer;
                let dimY = container.offsetHeight;
                let dimX = container.offsetWidth;
                let scale = this.stage.scaleX(); //as we have linear scale to both axis
                let konvaJson = this.stage.toJSON();
                let poss = [];
                for(let i = 0; i < this.concepts.length; i++){
                    poss[i] = this.concepts[i].konvaNode.position();
                }
                let duration = performance.now() - this.timeStamp;
                this.timeStamp = 0;
                console.log(this.clicksPerConcept);
                fetch("api/spatial/next", {
                    method: 'POST',
                    body: JSON.stringify({
                        dimX: dimX,
                        dimY: dimY,
                        duration: duration,
                        scale: scale,
                        konvaJson: konvaJson,
                        positions: poss,
                        clicksPerConcept: this.clicksPerConcept
                    })
                })
                    .then(res => res.json())
                    .then(json => {
                        if (json.moreData === false) location.href = '.';
                        else location.reload();
                    });
            }
        }
    });
</script>
