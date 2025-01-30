chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.type === 'fetchWeekData') {
        fetch('https://codeforcesdq.onrender.com/api/v1/latestWeek')
            .then(response => response.json())
            .then(data => {
                sendResponse({ success: true, data: data });
            })
            .catch(error => {
                sendResponse({ success: false, error: error.message });
            });
        return true; 
    }

    if(request.type === 'createNewWeek'){
        console.log(request.data);
        fetch('https://codeforcesdq.onrender.com/api/v1/createweek', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'handle':request.data.handle,
                'key':request.data.key,
                'secret':request.data.secret,
                'tag':request.data.tag
            },
            body: JSON.stringify(request.data),

            
        })
            .then(response => response.json())
            .then(data => {
                sendResponse({ success: true, data: data });
            })
            .catch(error => {
                sendResponse({ success: false, error: error.message });
            });

        console.log("hi",request.data);
        return true;

    }

    if(request.type==='markSubmitted'){
        fetch('https://codeforcesdq.onrender.com/api/v1/markSolved', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'handle':request.data.handle,
                'key':request.data.key,
                'secret':request.data.secret,
                'url':request.data.url,
                'submissionUrl':request.data.submissionUrl
            },
            body: JSON.stringify(request.data),
        })
          
            .then(response => response.text())
            .then(data => {
                sendResponse({ success: true, data: data });
            })
            .catch(error => {
                sendResponse({ success: false, error: error.message });
            });
        return true;
    }
});