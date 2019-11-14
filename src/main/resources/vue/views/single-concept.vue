<template id="single-concept">
        <div class="single-concept">
            <span class="single-concept-text"><slot></slot></span>
        </div>
</template>

<script>
    Vue.component("single-concept",{
        template: "#single-concept"
    });
</script>
<link type="text/css" rel="stylesheet" href="css/single-concept.css">