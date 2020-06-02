Vue.use(bootstrapVue);
var vm = new Vue({
    el: "#main-vue",
    data: {
        consts: {
            impress: "#",
            privacy: "#"
        },
        completedPoll: " ",
        interval: null,
        showLogin: true
    },
    created() {
        fetch("api/consts")
            .then(res => res.json())
            .then(json => {
                this.consts.impress = json.impress;
                this.consts.privacy = json.privacy;
            });
        this.loadData();
        let self = this;
        self.interval = setInterval(function () {
            self.loadData();
        }, 10000);
    },
    methods: {
        loadData: function () {
            fetch("info/completedpolls")
                .then(res => res.json())
                .then(json => {
                    let x = json.completedPoll;
                    this.completedPoll = json.completedPoll;
                });
            this.isAdminFunc()
        },
        isAdminFunc: function () {
            this.showLogin = !this.$javalin.state.isAdmin;
        },
        beforeDestroy() {
            clearInterval(this.interval)
        }
    }
});