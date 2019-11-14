<template id="compare-concepts">
    <div>
            <div class="col col-sm-6">
                <single-concept><b-spinner v-if='concept1 === "" '></b-spinner>{{concept1}}</single-concept>
            </div>
            <div class="col col-sm-6 col-">
                <single-concept><b-spinner v-if='concept2 === "" '></b-spinner>{{concept2}}</single-concept>
            </div>
    </div>
</template>

<script>
    Vue.component("compare-concepts", {
        template: "#compare-concepts",
        props: ['concept1', 'concept2']
    });
</script>