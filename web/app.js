const KEY = "steplab-demo-records-v1";
const $ = id => document.getElementById(id);
const today = new Date();
$("date").value = new Date(today.getTime()-today.getTimezoneOffset()*60000).toISOString().slice(0,10);
function load(){try{return JSON.parse(localStorage.getItem(KEY)||"[]")}catch{return []}}
function save(rows){localStorage.setItem(KEY,JSON.stringify(rows))}
function render(){
  const rows=load().sort((a,b)=>b.date.localeCompare(a.date));
  const sum=rows.reduce((n,r)=>n+r.count,0);
  $("total").textContent=sum.toLocaleString()+" demo steps";
  $("records").innerHTML=rows.length?rows.map(r=>`<div class="record"><div><strong>${r.date}</strong><small>Local browser demo record</small></div><b>${r.count.toLocaleString()}</b></div>`).join(""):'<p class="empty">No demo records yet.</p>';
}
$("add").addEventListener("click",()=>{
  const count=Number($("count").value), date=$("date").value;
  if(!Number.isInteger(count)||count<1||count>100000||!date){$("status").textContent="Enter a valid count (1–100,000) and date.";return}
  const rows=load();rows.push({count,date,id:crypto.randomUUID?.()||String(Date.now())});save(rows);
  $("status").textContent="Demo record added to this browser only.";render();
});
$("clear").addEventListener("click",()=>{localStorage.removeItem(KEY);$("status").textContent="Local demo data cleared.";render()});
render();
