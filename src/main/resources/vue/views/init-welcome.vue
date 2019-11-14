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
                <b-button variant="primary" size="lg" block v-bind:href="pollUrl">
                    {{buttonText}}
                </b-button>
            </div>
        </div>
        <div class="row mt-2">
            <div class="col col-12">
                <b-button variant="secondary" size="md" block href="testpoll">
                    Spatial Poll (test version)
                </b-button>
            </div>
        </div>
    </div>
</template>

<script>
    Vue.component("init-welcome", {
        template: "#init-welcome",
        data: function() {
            var btnText;
            var url = 'poll';
            if(this.$javalin.state.finished) {
                btnText = "Start new Poll";
                url = 'invalidate';
            }
            else if(this.$javalin.state.init) btnText = "Continue Poll";
            else btnText = "Start Poll";
            return {
                welcomeText: "",
                buttonText: btnText,
                pollUrl: url
            }
        },
        created(){
            fetch("api/consts?key=welcomeText")
                .then(res => res.json())
                .then(res => this.welcomeText = res.welcomeText)
        }
    });
</script>

