var exonym = {

	target: null,

	authCallback: null, 

	init: (authCallback) => {
		const wrapper = document.getElementById("exonymAuthWrapper");
		exonym.target = wrapper.getAttribute("endpoint");		
		exonym.authCallback = authCallback;
		
		if (exonym.target==null || exonym.target =="null"){
			console.error("Please set endPoint attribute on the exonymAuthWrapper");

		} else {		
			const signInBtn = document.getElementById("exonymSignInButton");
			signInBtn.addEventListener("click", exonym.sendProbe);
			exonym.auth(exonym.target, exonym.insertAuthRequest);

		}
	},
	
	auth: (endPoint, callback) => {
		callback(exonym.get(endPoint));
	},

	sendProbe: () =>{
		exonym.swapVisibility();
		exonym.post(exonym.target, {probe:true})
		    .then(exonym.handleTimeout);

	}, 

	insertAuthRequest: async (json) =>  {	
		json = await json;
		if (json.auth==true){
			exonym.executeCallback();

		} else {
			exonym.showAuthWrapper(true);
			var source = "data:image/png;base64,--qr-base64--";
			source = source.replace("--qr-base64--", json.qrPngB64);
			exonym.displayQr(source, "exonymQrDisplay");
			exonym.addLink(json.link, "exonymLink");
	
		}
	},

	handleTimeout: (resp) => {
		if (resp.timeout){
			exonym.swapVisibility();
			exonym.auth(exonym.target, exonym.insertAuthRequest);

		} else if (resp.auth) {
			exonym.swapVisibility();
			exonym.executeCallback();
		}
	},

	executeCallback: () => {
		exonym.showAuthWrapper(false);
		if (typeof exonym.authCallback == "function"){
			exonym.authCallback();

		}
	},

	showAuthWrapper: (showBoolean) => {
		const wrapper = document.getElementById("exonymAuthWrapper");
		if (showBoolean){
			wrapper.classList.remove("exonymHide");

		} else {
			wrapper.classList.add("exonymHide");

		}
	},

	swapVisibility: () => {
		const invite = document.getElementById("exonymInvite");
		const qr = document.getElementById("exonymProbeWrapper");
		if (qr.classList.contains("exonymHide")){
			qr.classList.remove("exonymHide");
			invite.classList.add("exonymHide");
	
		} else {
			qr.classList.add("exonymHide");
			invite.classList.remove("exonymHide");

		}
	},

	displayQr(source, target){		
		document.getElementById(target).setAttribute("src", source);
		
	},

	addLink(link, target){
		document.getElementById(target).setAttribute("href", link);
		

	},

	get: async (url = '') => {
		const response = await fetch(url, {
		  method: 'GET', // *GET, POST, PUT, DELETE, etc.
		  mode: 'no-cors', // no-cors, *cors, same-origin
		  cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
		  credentials: 'same-origin', // include, *same-origin, omit
		  headers: {
			'Content-Type': 'application/json'
		  },
		  redirect: 'error', // manual, *follow, error
		  referrerPolicy: 'strict-origin-when-cross-origin', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
		});
		return response.json().catch((e) => {
			console.error("Error at GET follows");
			console.log(e);
	
		});    
	},

	post: async (url = '', data = {}) => {
		const response = await fetch(url, {
		  method: 'POST', // *GET, POST, PUT, DELETE, etc.
		  mode: 'no-cors', // no-cors, *cors, same-origin
		  cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
		  credentials: 'same-origin', // include, *same-origin, omit
		  headers: {
			'Content-Type': 'application/json'
		  },
		  redirect: 'error', // manual, *follow, error
		  referrerPolicy: 'strict-origin-when-cross-origin', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
		  body: JSON.stringify(data)
		});
		return response.json().catch((e) => {
			console.error("Error at POST follows");
			console.error(e);
	
		});
	},
	
	ready: (callback) => {
		if (document.readyState!='loading') {
			callback();	
			
		} else if (document.addEventListener) { 
			document.addEventListener('DOMContentLoaded', callback);
			
		} else { 
			document.attachEvent('onreadystatechange', () => {			
				if (document.readyState=='complete') { 
					callback();
				}
			});
		}
	}	
}
