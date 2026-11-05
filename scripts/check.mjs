import {execFileSync} from 'node:child_process';
import {readFileSync} from 'node:fs';
for (const f of ['app.js','diagram.js','native.js']) execFileSync(process.execPath,['--check','web/'+f],{stdio:'inherit'});
const page=readFileSync('web/index.html','utf8');
for(const f of ['app.js','diagram.js','native.js']) if(!page.includes(f)) throw Error('Missing script '+f);
console.log('Web syntax and entry points OK');
