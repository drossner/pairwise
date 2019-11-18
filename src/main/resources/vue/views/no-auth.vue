<template id="no-auth">
    <div class="row mt-2">
        <div class="col col-12">
           <h1>Not (yet) authorized!</h1>
            <p>To see this (and your) overview, finish all offered polls.</p>
            <b-button href="." variant="primary">Back to Home</b-button>
        </div>
    </div>
</template>

<script>
    Vue.component("no-auth", {
        template: "#no-auth"
    })
</script>

