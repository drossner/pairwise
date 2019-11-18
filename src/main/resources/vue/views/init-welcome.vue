<template id="init-welcome">
    <div>
        <div class="row">
            <div class="col col-12">
                <h1>Welcome to Pairwise</h1>
            </div>
        </div>
        <div class="row mt-2">
            <div class="col col-12">
                <p>{{welcomeText}}</p>
                <b-button variant="outline-primary" size="lg" block :disabled="compBtn.disabled" :href="compBtn.url">
                    {{compBtn.text}}
                </b-button>
            </div>
        </div>
        <div class="row mt-2">
            <div class="col col-12">
                <b-button variant="outline-primary" size="lg" block :disabled="spatBtn.disabled" :href="spatBtn.url">
                    {{spatBtn.text}}
                </b-button>
            </div>
        </div>
        <div class="row mt-1">
            <div class="col col-12">
                <p v-if="!(allowAdmin)" class="text-info">
                    Please finish all polls to see your result and those of other participants.
                </p>
                <p v-else class="text-success">
                    Press the buttons above to see your result. Go to the <a href="admin">Evaluation</a> to see an overview.
                </p>
            </div>
        </div>
    </div>
</template>

<script>
    Vue.component("init-welcome", {
        template: "#init-welcome",
        data: function () {
            return {
                welcomeText: "",
            }
        },
        computed: {
            compBtn: function () {
                let text = "Start Poll";
                let url = "poll";
                let state = this.$javalin.state;
                let disabled = false;
                if (state.finished) {
                    text = "Thanks for your participation";
                    url = "admin/comp/" + state.compId;
                    if(!this.allowAdmin) disabled = true;
                } else if (state.init) {
                    text = "Continue Poll"
                }
                if(this.allowAdmin) text = "See your poll result";
                return {text: text, url: url, disabled: disabled};
            },
            spatBtn: function () {
                let text = "Start Spatial Poll";
                let url = "testpoll";
                let state = this.$javalin.state;
                let disabled = false;
                if (state.finishedSpat) {
                    text = "Thanks fou your participation";
                    url = "admin/spat/" + state.spatId;
                    if(!this.allowAdmin) disabled = true;
                } else if (state.initSpat) {
                    text = "Continue Spatial Poll"
                }
                if(this.allowAdmin) text = "See your spatial poll result";
                return {text: text, url: url, disabled: disabled}
            },
            allowAdmin: function () {
                let state = this.$javalin.state;
                return state.finished && state.finishedSpat;
            }
        },
        created() {
            fetch("api/consts?key=welcomeText")
                .then(res => res.json())
                .then(res => this.welcomeText = res.welcomeText)
        }
    });
</script>

