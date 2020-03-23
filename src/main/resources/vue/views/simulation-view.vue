<template id="simulation-view">
    <div class="row spatial-row mt-2 position-relative">
        <div class="col col-12" id="canvasContainer" ref="canvasContainer"></div>
        <div class="col-6">
            <b-form-input id="speed-range"
                          v-model="speed"
                          type="range"
                          min="0.1"
                          max="1"
                          step="0.1">
            </b-form-input>
        </div>
        <div class="mt-2 col-6">Geschwindigkeit: {{ speed }}</div>
    </div>
</template>

<script>
    Vue.component("simulation-view", {
        template: '#simulation-view',
        data: function(){
            return {
                stage: null,
                concepts: [],
                fullStorage: [],
                minDist: 99999999999,
                maxDist: 0,
                distStep: 0,
                world: null,
                boxEdges: [],
                lines: [],
                speed: .1
            }
        },
        mounted(){
            //this.createPollCanvas(this.$refs.canvasContainer);
            this.resize();
        },
        created(){
            window.addEventListener("resize", this.resize);
            fetch("admin/api/spatial/simulationdata")
                .then(res => res.json())
                .then(json => {
                    this.fullStorage = json;
                    for(let i = 0; i < this.fullStorage.length; i++){
                        if(!this.concepts.some(elem => elem.name === this.fullStorage[i].a))
                            this.concepts.push({name: this.fullStorage[i].a});
                        if(!this.concepts.some(elem => elem.name === this.fullStorage[i].b))
                            this.concepts.push({name: this.fullStorage[i].b});
                        this.minDist = Math.min(this.minDist, this.fullStorage[i].dist);
                        this.maxDist = Math.max(this.maxDist, this.fullStorage[i].dist);
                    }
                    this.distStep = this.maxDist / 10;
                    this.createPollCanvas(this.$refs.canvasContainer);
                    this.resize();
                    this.stage.draw();
                    return this;
                }).then(self => {
                Box2D().then(function (ph) {
                    self.world = new ph.b2World(new ph.b2Vec2(0.0, 0.0)); //0 gravity world
                    //fix factor of 10 like 100px = 10meter
                    let cam = 10;
                    //create bodies:
                    for(let i = 0; i < self.concepts.length; i++) {
                        let kon = self.concepts[i].konvaNode;
                        let konPos = self.getGroupPos(kon);
                        let konBox = self.getGroupBox(kon);
                        let bodyDef = new ph.b2BodyDef();
                        bodyDef.set_type(ph.b2_dynamicBody);
                        bodyDef.set_fixedRotation(true);
                        bodyDef.set_position(new ph.b2Vec2(konPos.x / cam, konPos.y / cam));
                        bodyDef.set_linearDamping(9);
                        bodyDef.set_angularDamping(0.9);

                        let shape = new ph.b2CircleShape();
                        shape.set_m_radius((konBox.width/2) / cam);

                        let fixture = new ph.b2FixtureDef();
                        fixture.set_density(2); //2
                        fixture.set_friction(0.2); //0.2
                        fixture.set_restitution(0.9); //0.9
                        fixture.set_shape(shape);
                        fixture.set_isSensor(false);

                        let body = self.world.CreateBody(bodyDef);
                        //body.set_linearDamping(9);
                        //body.set_angularDamping(0.9);
                        body.CreateFixture(fixture); //does not work..
                        self.concepts[i].box2dNode = body;
                    }
                    for(let i = 0; i < self.fullStorage.length; i++){
                        let edge = self.fullStorage[i];
                        let bodyA = self.concepts.find(elem => elem.name === edge.a);
                        let bodyB = self.concepts.find(elem => elem.name === edge.b);
                        let line = self.lines.find(line => line.ident.includes(bodyA.name) && line.ident.includes(bodyB.name));
                        self.boxEdges.push({a: bodyA, b: bodyB, line: line, dist: edge.dist});
                        bodyA = bodyA.box2dNode;
                        bodyB = bodyB.box2dNode;
                        let jointDef = new ph.b2DistanceJointDef();
                        jointDef.set_bodyA(bodyA);
                        jointDef.set_bodyB(bodyB);
                        jointDef.set_frequencyHz(3.0);
                        jointDef.set_length(edge.dist / cam);
                        jointDef.set_dampingRatio(0.5); //0.5

                        self.world.CreateJoint(jointDef);
                    }

                    //start gameloop (OMG)
                    let interval = 50;
                    let origFrames = (1000/(interval*self.speed));
                    let run = setInterval(request, interval);
                    function request() {
                        self.world.Step(1/origFrames, 4, 4); // as in android, max iteration collision, max pos iteration
                        //move konva stuff..
                        for(let i = 0; i < self.concepts.length; i++){
                            let con = self.concepts[i];
                            let nX = con.box2dNode.GetPosition().get_x() * cam;
                            let nY = con.box2dNode.GetPosition().get_y() * cam;
                            con.konvaNode.absolutePosition({x: nX, y: nY});
                        }
                        //lines..
                        for(let i = 0; i < self.boxEdges.length; i++){
                            let a = self.boxEdges[i].a;
                            let b = self.boxEdges[i].b;
                            let line = self.boxEdges[i].line;
                            let dist = self.boxEdges[i].dist;
                            self.updateLine.apply(self, [a.konvaNode, b.konvaNode, line, dist]);
                        }
                        self.stage.batchDraw();
                        clearInterval(run);
                        origFrames = (1000/(interval*self.speed));

                        run = setInterval(request, interval);
                        console.log(origFrames);
                    }
                });
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
                let concepts = this.concepts.map(concept => concept.name); //todo
                let nodes = [];
                let self = this;
                for(let i = 0; i < concepts.length; i++){
                    nodes[i] = this.createNode(concepts[i], Math.random()*this.stage.width(), Math.random()*this.stage.height());
                    this.concepts[i].konvaNode = nodes[i];
                }
                //console.info(concepts);
                this.lines = this.createLines(nodes);

                nodes.forEach(value => {
                    mainLayer.add(value);
                });

                this.lines.forEach(value => {
                    lineLayer.add(value);
                });

                stage.add(lineLayer);
                stage.add(mainLayer);
            },

            createLines: function (nodeArr) {
                let lineArr = [];
                for(let i = 0; i < nodeArr.length-1; i++){
                    for(let k = i + 1; k < nodeArr.length; k++){
                        //line between i and k
                        let nk = nodeArr[k], ni = nodeArr[i];
                        let iBox = this.getGroupBox(ni);
                        let kBox = this.getGroupBox(nk);
                        let line = new Konva.Line({
                            points: [
                                nk.x(), nk.y(),
                                ni.x(), ni.y()], //x,y , x,y , ... take the middle
                            stroke: 'lightgray',
                            strokeWidth: 2,
                        });
                        //get ideal distance
                        let idealDistance = 0;
                        for(let i = 0; i < this.fullStorage.length; i++){
                            let tmp = [this.fullStorage[i].a, this.fullStorage[i].b];
                            if(tmp.includes(nk.textContent) && tmp.includes(ni.textContent)){
                                idealDistance = this.fullStorage[i].dist; break;
                            }
                        }
                        let self = this;
                        nk.on('dragmove', function(){
                            self.updateLine.apply(self, [nk, ni, line, idealDistance]);
                        });
                        ni.on('dragmove', function(){
                            self.updateLine.apply(self, [nk, ni, line, idealDistance]);
                        });
                        line.ident = [nk.textContent, ni.textContent];
                        lineArr.push(line);
                    }
                }
                return lineArr;
            },
            createNode: function (text, x, y) {
                const width = 100, height = 60;
                const group = new Konva.Group({
                    x: x, y: y,
                    draggable: true
                });

                let rect = new Konva.Circle({
                    x: 0, y: 0,
                    width: height, height: height,
                    stroke: 'black', strokeWidth: 1, fill: 'white'
                });

                let content = new Konva.Text({
                    x: -height/2, y: -6,
                    width: height, height: height,
                    text: text,
                    //padding: 10,
                    fontSize: 12,
                    align: 'center'
                });

                group.add(rect);
                group.add(content);
                group.textContent = text;
                return group;
            },
            updateLine: function (nodeA, nodeB, line, idealDistance) {
                let aBox = this.getGroupBox(nodeA);
                let bBox = this.getGroupBox(nodeB);
                line.points(
                    [
                        nodeA.x(), nodeA.y(),
                        nodeB.x(), nodeB.y()]
                );
                let fstA = line.points().slice(0, 2);
                let sndA = line.points().slice(2, 4);
                let length = Math.sqrt(Math.pow(fstA[0]-sndA[0], 2) + Math.pow(fstA[1]-sndA[1], 2));
                let minStroke = 0.1;
                let maxStroke = 80;
                //1000 => 0.1
                //60 => 80
                let normedLength = Math.min(length, 1000);
                normedLength = Math.max(normedLength, 60);
                let normedStroke = (-0.03993403403) * normedLength + 40; //(2162/27); //Mathemagie (leicht verzaubert für 60..
                let stroke = Math.min((1/length)*100, 50);
                //line.strokeWidth(normedStroke/2); //1118 vertical px in 1000 * 500
                line.strokeWidth(3); //1118 vertical px in 1000 * 500
                //calc delta and stroke
                //let delta = Math.min(Math.abs(length - idealDistance), this.maxDist); //delta max = max distance, delta min = 0
                //let deltaGroup = Math.round(delta / this.distStep); // 0..9
                let delta = Math.abs(length - idealDistance);
                let deltaGroup = Math.round((delta / idealDistance) * 10);
                let color = "lightgray";
                let opac = 1;
                switch (deltaGroup) {
                    case 0: opac = 0.5; color = "#1AE600"; break;
                    case 1: opac = 0.55; color = "#33CC00"; break;
                    case 2: opac = 0.6; color = "#4DB300"; break;
                    case 3: opac = 0.65; color = "#669900"; break;
                    case 4: opac = 0.7; color = "#808000"; break;
                    case 5: opac = 0.75; color = "#996600"; break;
                    case 6: opac = 0.8; color = "#B34D00"; break;
                    case 7: opac = 0.85; color = "#CC3300"; break;
                    case 8: opac = 0.9; color = "#E61A00"; break;
                    case 9: opac = 1.0; color = "#FF0000"; break;
                    default: color = "#FF0000"
                }
                line.stroke(color);
                line.opacity(opac);
                line.getLayer().batchDraw();
            },
            getGroupBox: function (group) {
                return group.getChildren()[0].size();
            },
            getGroupPos: function (group) {
                return group.getChildren()[0].absolutePosition();
            }
        }
    });
</script>
