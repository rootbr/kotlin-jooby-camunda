import pkg from './package.json';

import cjs from 'rollup-plugin-commonjs';
import vue from 'rollup-plugin-vue';

function pgl() {
  return [
    cjs(),
    vue()
  ];
}

export default [
  {
    input: './src/main/javascript/index.vue',
    output: {
      name: 'VueBpmn',
      file: `public/js/vue-bpmn.umd.js`,
      format: 'umd'
    },
    plugins: pgl()
  },
  {
    input: './src/main/javascript/table-processes.vue',
    output: {
      name: 'VueBootstrapTable',
      file: `public/js/vue2-bootstrap-table2.umd.js`,
      format: 'umd'
    },
    plugins: pgl()
  },
  {
    input: './src/main/javascript/index.vue',
    output: [
      { file: pkg.main, format: 'cjs' },
      { file: pkg.module, format: 'es' }
    ],
    plugins: pgl()
  }
];
