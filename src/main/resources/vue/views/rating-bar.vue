<template id="rating-bar">
        <b-form-group>
            <b-form-radio-group class="d-flex flex-wrap"
                v-model="selected"
                :options="options"
                buttons
                button-variant="outline-primary"
                v-on:change="selectionChanged($event)"
                ></b-form-radio-group>
        </b-form-group>
</template>

<script>
    Vue.component("rating-bar", {
        template: "#rating-bar",
        data: function() {
            return {
                selected: '0',
                options: [
                    {text: 'Please rate', value: '0'},
                    {text: '1', value: '1'},
                    {text: '2', value: '2'},
                    {text: '3', value: '3'},
                    {text: '4', value: '4'},
                    {text: '5', value: '5'},
                    {text: '6', value: '6'},
                    {text: '7', value: '7'},
                    {text: '8', value: '8'},
                    {text: '9', value: '9'},
                    {text: '10', value: '10'},
                ]
            }
        },
        methods: {
            selectionChanged: function(value) {
                this.$emit('is-rated', value > 0);
            }
        }
    });
</script>