/* 31-Day Reset service worker. (c) 2026 Akinwande Akinkunmi Adeniyi. All rights reserved. */
const C='reset31-v4-1';
const F=['./','index.html','manifest.webmanifest','icon-192.png','icon-512.png','icon-180.png'];
const T=[["Cardio (20 min)", "Hydrate check + baseline"], ["Strength (intro)", "Sleep routine"], ["Brisk walk (30 min)", "Fibre check"], ["Lower body", "Unwind (10 min)"], ["Cardio (20-25 min)", "Hydrate check"], ["Full body circuit", "Sleep routine"], ["Mobility (25 min)", "Plan and prep meals"], ["Upper body", "Hydrate check"], ["Zone 2 cardio (30-35 min)", "Fibre check"], ["Lower body + core", "Evening check-in"], ["Brisk walk (30-40 min)", "Breathe (10 min)"], ["Full body circuit", "Sleep routine"], ["Cardio (25 min)", "Fibre check"], ["Recover (easy walk + mobility)", "Halfway review"], ["Full body + push-up retest", "Progress log"], ["Cardio + core", "Evening check-in"], ["Lower body + walk", "Evening check-in"], ["Mobility (25 min)", "Sleep routine"], ["Upper body", "Unwind (10 min)"], ["Intervals", "Hydrate check"], ["Active recovery", "Fibre check + meal plan"], ["Full body circuit (4 rounds)", "Whole foods check"], ["Cardio (30 min)", "Sleep routine"], ["Lower body + core", "Evening check-in"], ["Walk (40 min)", "Digest (gut check)"], ["Upper body", "Hydrate check"], ["Cardio stamina test", "Evening check-in"], ["Recover (easy walk + mobility)", "Sleep routine"], ["Full body + finisher", "Evening check-in"], ["Cardio (30 min)", "Reflect"], ["Move + final retest", "Final results + celebrate"]];
self.addEventListener('install',e=>{e.waitUntil(caches.open(C).then(c=>c.addAll(F)));self.skipWaiting();});
self.addEventListener('activate',e=>{e.waitUntil(caches.keys().then(k=>Promise.all(k.filter(x=>x!==C).map(x=>caches.delete(x)))));self.clients.claim();});
self.addEventListener('fetch',e=>{if(e.request.method!=='GET')return;e.respondWith(fetch(e.request).then(r=>{const cp=r.clone();caches.open(C).then(c=>c.put(e.request,cp));return r;}).catch(()=>caches.match(e.request,{ignoreSearch:true}).then(r=>r||caches.match('index.html'))));});
function db(){return new Promise((res,rej)=>{const r=indexedDB.open('reset31',1);r.onupgradeneeded=()=>r.result.createObjectStore('kv');r.onsuccess=()=>res(r.result);r.onerror=()=>rej(r.error);});}
function get(k){return db().then(d=>new Promise(res=>{const q=d.transaction('kv').objectStore('kv').get(k);q.onsuccess=()=>res(q.result);q.onerror=()=>res(undefined);}));}
function put(k,v){return db().then(d=>new Promise(res=>{const t=d.transaction('kv','readwrite');t.objectStore('kv').put(v,k);t.oncomplete=()=>res();t.onerror=()=>res();}));}
function mins(t){const p=t.split(':');return (+p[0])*60+(+p[1]);}
async function nudge(){
  const s=await get('state'); if(!s||!s.cfg||!s.cfg.start) return;
  const p=s.cfg.start.split('-'), st=new Date(+p[0],+p[1]-1,+p[2]), now=new Date();
  const n=Math.floor((new Date(now.getFullYear(),now.getMonth(),now.getDate())-st)/86400000)+1;
  if(n<1||n>31) return;
  const d=(s.days&&s.days[n])||{}, m=now.getHours()*60+now.getMinutes(), w=mins(s.cfg.wt), e=mins(s.cfg.et);
  let slot,title,body;
  if(m<w){slot='am';title='Today: Day '+n+' of 31';body=T[n-1][0]+' at '+s.cfg.wt+', then '+T[n-1][1]+' at '+s.cfg.et+'.';}
  else if(m<e&&!d.w){slot='w';title='Day '+n+': '+T[n-1][0];body='Have you done today\'s workout? Tap for the instructions.';}
  else if(m>=e&&!d.e){slot='e';title='Day '+n+': '+T[n-1][1];body='Your evening habit is waiting. Tap to tick it off.';}
  else return;
  const key='sent-'+n+slot; if(await get(key)) return; await put(key,1);
  return self.registration.showNotification(title,{body,tag:'reset-'+n+slot,icon:'icon-192.png',badge:'icon-192.png',data:{url:'./'}});
}
self.addEventListener('periodicsync',e=>{if(e.tag==='reset31-daily')e.waitUntil(nudge());});
self.addEventListener('notificationclick',e=>{e.notification.close();e.waitUntil(clients.matchAll({type:'window',includeUncontrolled:true}).then(l=>{for(const c of l){if('focus' in c)return c.focus();}return clients.openWindow('./');}));});
