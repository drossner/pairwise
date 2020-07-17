<template id="csv-downloader">
    <b-button block href="admin/api/download" size="lg" variant="outline-primary">Download results as csv</b-button>
</template>

<script>
    Vue.component("csv-downloader", {
        template: "#csv-downloader",
        data: function () {
            return {

            }
        },
        methods: {
            download: function () {
                fetch("admin/api/download")
            }
        }
    });
</script>