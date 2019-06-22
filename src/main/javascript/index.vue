<template>
    <div ref="container" class="vue-bpmn-diagram-container"></div>
</template>

<script>
    import BpmnJS from 'bpmn-js/dist/bpmn-navigated-viewer.production.min.js';

    export default {
        name: 'vue-bpmn',
        props: {
            url: {
                type: String,
                required: true
            }
        },
        data: function () {
            return {
                diagramXML: null,
                activities: null
            };
        },
        mounted: function () {
            var container = this.$refs.container;

            var self = this;

            this.bpmnViewer = new BpmnJS({
                container: container
            });

            this.bpmnViewer.on('import.done', function (event) {

                var error = event.error;
                var warnings = event.warnings;

                if (error) {
                    self.$emit('error', error);
                } else {
                    self.$emit('shown', warnings);
                }

                self.bpmnViewer.get('canvas').zoom('fit-viewport');

                fetch('http://localhost:8080/api/activities')
                    .then(function(response) {
                        return response.json();
                    })
                    .then(function(myJson) {
                        console.log(JSON.stringify(myJson));
                        self.activities = myJson
                    });

            });

            if (this.url) {
                this.fetchDiagram(this.url);
            }


        },
        beforeDestroy: function () {
            this.bpmnViewer.destroy();
        },
        watch: {
            url: function (val) {
                this.$emit('loading');
                this.fetch(url);
            },
            diagramXML: function (val) {
                this.bpmnViewer.importXML(val);
            },
            activities: function (val) {
                let overlays = this.bpmnViewer.get('overlays');
                for (var key in val) {
                    let element = val[key];
                    console.log(key + ':' + element);
                    overlays.add(key, {
                        position: {
                            bottom: 0,
                            right: 0
                        },
                        html: '<div class="success-message">' + val[key] + '</div>'
                    });
                }
            }
        },
        methods: {
            fetchDiagram: function (url) {
                var self = this;
                fetch(url)
                    .then(function (response) {
                        return response.text();
                    })
                    .then(function (text) {
                        self.diagramXML = text;
                    })
                    .catch(function (err) {
                        self.$emit('error', err);
                    });
            }
        }
    };
</script>

<style>
    .vue-bpmn-diagram-container {
        height: 100%;
        width: 100%;
    }

    .success-message {
        color: red;
        text-shadow: 0 0 black;
        background-color: floralwhite;
    }
</style>
