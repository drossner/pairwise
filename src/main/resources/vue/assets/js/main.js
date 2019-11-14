Vue.use(bootstrapVue);
var vm = new Vue({
    el: "#main-vue",
    data: {
        consts: {
            impress: "#",
            privacy: "#"
        }
    },
    created () {
        fetch("api/consts")
            .then(res => res.json())
            .then(json => {
                this.consts.impress = json.impress;
                this.consts.privacy = json.privacy;
            })

    }
});