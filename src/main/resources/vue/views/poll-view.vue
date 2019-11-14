<template id="poll-view">
    <div>
        <b-alert
                v-model="alert.state"
                class="position-fixed fixed-bottom m-0 rounded-0"
                style="z-index: 9999;"
                variant="danger"
                dismissible
                @dismissed="alert.state = 0"
        >{{alert.text}}</b-alert>
        <compare-concepts class="row mt-4" v-bind:concept1=poll.comparisons[poll.currQst].conceptA
                          v-bind:concept2=poll.comparisons[poll.currQst].conceptB></compare-concepts>

        <div class="row align-items-center mt-3">
            <rating-bar class="col col-md-8 col-sm-12 col-12 offset-md-2"
                v-on:is-rated="allowNext=$event"
                ref="ratingbarchild"></rating-bar>
        </div>

        <div class="row mt-2">
            <div class="col col-md-8 col-sm-12 col-12 offset-md-2">
                <b-button block size="lg" variant="primary"
                    :disabled=!allowNext
                    v-on:click="nextComp"
                >
                    {{btnText}}
                </b-button>
            </div>
        </div>
        <div class="row mt-4">
            <div class="col col-12">
                <b-progress
                        variant="secondary"
                        :max="poll.comparisons.length"
                        height="30px"
                >
                    <b-progress-bar :hidden="!ready"
                            :value="poll.currQst + 1"
                    >
                        Progress: <strong>{{poll.currQst + 1}} / {{poll.comparisons.length}}</strong>
                    </b-progress-bar>
                </b-progress>
            </div>
        </div>
    </div>
</template>

<script>
    Vue.component("poll-view", {
        template: "#poll-view",
        data: () => {
           return  {
               poll: {
                   comparisons: [{
                       conceptA: "", conceptB: ""
                   }],
                   currQst: 0
               },
               allowNext: false,
               btnText: "Next",
               alert: {
                   state: 0,
                   text: "Something went wrong"
               },
               timeStamp: 0,
               ready: false
            }
        },
        created () {
            fetch("api/poll")
                .then(res => res.json())
                .then(json => {
                    this.poll = json;
                    this.timeStamp = performance.now();
                    this.ready = true;
                });
        },
        methods: {
            nextComp: function () {
                var rating = this.$refs.ratingbarchild.selected;
                this.allowNext = false;
                fetch("api/next?qstNr="+this.poll.currQst, {
                    method: 'POST',
                    header: {'Content-Type': 'application/json'},
                    body: JSON.stringify({
                        rating: rating,
                        duration: performance.now()-this.timeStamp
                    })
                })
                    .then(res => Promise.all([res.text(), Promise.resolve(res.status)]))
                    .then(resArr => {
                        let res = {text: resArr[0], status: resArr[1]};
                        if(Math.floor(res.status/100) === 2){
                            this.$refs.ratingbarchild.selected = 0;
                            if(this.poll.currQst + 1 >= this.poll.comparisons.length){
                                location.href = ".";
                                return;
                            } else if(this.poll.currQst + 2 >= this.poll.comparisons.length){
                                this.btnText = "Finish Poll";
                            }
                            this.poll.currQst++;
                            this.timeStamp = performance.now();
                        }
                        else {
                            this.alert.text = res.text === null? "Server could not handle the request" : res;
                            this.alert.state = 8;
                        }
                    }
                );
            }
        }
    });
</script>