let hasTableBeenInserted = false;
let weekData = null;
let secret="";
let key="";
let handle="";
let isProcessingInsertion = false;
let checker=false;

async function insertDailyQuestionTable() {
    
    removeExistingTable();
  
    if (hasTableBeenInserted) {
        return;
    }

    const sidebar = document.getElementById("sidebar");
    if (!sidebar) {
        console.error('Sidebar not found');
        return;
    }

    try {
        if (!weekData) {
            const response = await getWeekdata();
            
            if (!response.data || response.data.length === 0) {
                //console.log("No data found - showing form");
                showform();
                return;
            }
            
            if (response && response.success) {
                weekData = response.data;
            }
        }

        if (!weekData || weekData.length === 0) {
            //console.log("No week data available - showing form");
            showform();
            return;
        }
    
        // Create a table container
        const tableContainer = document.createElement("div");
        tableContainer.className = "roundbox sidebox borderTopRound";
        tableContainer.id = "daily-question-extension";
        tableContainer.style.marginBottom = "10px";
    
        
        const weekTopic = weekData[0]?.mainTopic || "No Topic Set";
    
        tableContainer.innerHTML = `
            <div class="caption titled">→ <a href="http://localhost:8080/api/v1/allweeks?handle=${handle}" target="_blank" style="text-decoration: underline; cursor: pointer; color: inherit;">CFDQ</a>
            <button class="newWeek" style="float: right;">new</button>
            <button class="Refresh" style="float: right;">refresh</button>
            </div>
            <div style="padding: 0.5em;">
            <p style="font-size: 1.1em; font-weight: bold; margin-bottom: 0.5em;">Topic: ${weekTopic}</p>
            <table border="1">
                <thead>
                <tr>
                    <th>Days</th>
                    <th>Questions</th>
                    <th>Ans URL</th>
                    <th>Action</th>
                </tr>
                </thead>
                <tbody>
                ${weekData.map(({ url, code, status, mainTopic }, index) => `
                    <tr style="background-color: ${status === "solved" ? "lightgreen" : "white"};">
                    <td>${index + 1}</td>
                    <td><a href=${url || "#"} target="_blank">${code || "N/A"}</a></td>
                    <td>
                    <input type="url" 
                        placeholder="Enter Codeforces Submission URL" 
                        pattern="https:\/\/codeforces\.com\/contest\/\d+\/submission\/\d+" 
                        title="Enter a valid Codeforces submission URL" 
                        required 
                        style="width: 30px;" />
                    </td>
                    <td><button  class="mark-submitted" data-question="${url || ""}">Mark Submitted</button></td>
                    </tr>
                `).join("")}
                </tbody>
            </table>
            </div>
        `;
    
        sidebar.prepend(tableContainer);
        hasTableBeenInserted = true;
    
        
        addEventListeners(tableContainer);
    
    } catch (error) {
        console.error('Error in insertDailyQuestionTable:', error);
        
        showform();
    }
}

function addEventListeners(tableContainer) {
    
    const buttons = tableContainer.querySelectorAll(".mark-submitted");
    buttons.forEach((button) => {
        button.addEventListener("click", () => {
            const question = button.getAttribute("data-question");
            const input = button.closest("tr").querySelector("input[type='url']");
            if (validateUrl(input.value)) {
                markSubmitted(question,input.value);
            } else {
                alert("Please enter a valid Codeforces submission URL.");
            }
        });
    });

    async function markSubmitted(question,input) {
        try {
            const response = await chrome.runtime.sendMessage({
                type: 'markSubmitted',
                data: { url:question, handle, key, secret ,submissionUrl:input}
            });
            
            if (response.success) {
                //console.log('submitted:', response.data);
               
                refreshTable();
                return response;
            } else {
                //console.log('not submitted:', );
                return { success: false, data: [] };
            }
        } catch (error) {
            console.error('Error submittin data:', error);
            return { success: false, data: [] };
        }

    }

    const newWeekButton = tableContainer.querySelector(".newWeek");
    newWeekButton.addEventListener("click", showform);

    
    const refreshButton = tableContainer.querySelector(".Refresh");
    refreshButton.addEventListener("click", async () => {
        hasTableBeenInserted = false;
        weekData = null;
        // const oldTable = document.getElementById("daily-question-extension");
        // if (oldTable) oldTable.remove();
        // await insertDailyQuestionTable();
        refreshTable();
    });
}

async function getWeekdata() {
    try {
        const response = await chrome.runtime.sendMessage({
            type: 'fetchWeekData'
        });
        
        if (response.success && response.data) {
            //console.log('Data received:', response.data);
            return response;
        } else {
            console.log('No data received or empty response');
            return { success: false, data: [] };
        }
    } catch (error) {
        console.error('Error fetching week data:', error);
        return { success: false, data: [] };
    }
}

function showform() {
    //console.log('showform function called');
    
    const tableContainer = document.getElementById("daily-question-extension");
    //console.log('Found table container:', tableContainer);
    
    
    const formContainer = document.createElement("div");
    formContainer.id = "daily-question-form";
    

    const existingForm = document.getElementById("daily-question-form");
    if (existingForm) {
        //console.log('Removing existing form');
        existingForm.remove();
    }
    
    formContainer.innerHTML = `
        <div class="roundbox sidebox borderTopRound">
            <div class="caption titled">
                → <a href="http://localhost:8080/api/v1/allweeks?handle=${handle}" target="_blank" style="text-decoration: underline; cursor: pointer; color: inherit;">CFDQ</a>
            </div>
            <div style="padding: 0.5em;">
                <p style="font-size: 1.1em; font-weight: bold; margin-bottom: 0.5em;">Create a New Week</p>
                <form id="new-week-form">
                    <label for="handle">Handle:</label>
                    <input type="text" id="handle" name="handle" value="${handle}" required /><br><br>
                    
                    <label for="key">Key:</label>
                    <input type="text" id="key" name="key" value="${key}" required /><br><br>
                    
                    <label for="secret">Secret:</label>
                    <input type="text" id="secret" name="secret" value="${secret}" required /><br><br>
                    
                    <label for="topic">Topic:</label>
                    <select id="topic" name="topic" required>
                       <option value=""></option>
                        combine-tags-by-or
                       
                            <option value="2-sat" title="2-satisfiability">2-sat</option>
                            <option value="binary search" title="Binary search">binary search</option>
                            <option value="bitmasks" title="Bitmasks">bitmasks</option>
                            <option value="brute force" title="Brute force">brute force</option>
                            <option value="chinese remainder theorem" title="Chinese remainder theorem">chinese remainder theorem</option>
                            <option value="combinatorics" title="Combinatorics">combinatorics</option>
                            <option value="constructive algorithms" title="Constructive algorithms">constructive algorithms</option>
                            <option value="data structures" title="Heaps, binary search trees, segment trees, hash tables, etc">data structures</option>
                            <option value="dfs and similar" title="Dfs and similar">dfs and similar</option>
                            <option value="divide and conquer" title="Divide and Conquer">divide and conquer</option>
                            <option value="dp" title="Dynamic programming">dp</option>
                            <option value="dsu" title="Disjoint set union">dsu</option>
                            <option value="expression parsing" title="Parsing expression grammar">expression parsing</option>
                            <option value="fft" title="Fast Fourier transform">fft</option>
                            <option value="flows" title="Graph network flows">flows</option>
                            <option value="games" title="Games, Sprague-Grundy theorem">games</option>
                            <option value="geometry" title="Geometry, computational geometry">geometry</option>
                            <option value="graph matchings" title="Graph matchings, König's theorem, vertex cover of bipartite graph">graph matchings</option>
                            <option value="graphs" title="Graphs">graphs</option>
                            <option value="greedy" title="Greedy algorithms">greedy</option>
                            <option value="hashing" title="Hashing, hashtables">hashing</option>
                            <option value="implementation" title="Implementation problems, programming technics, simulation">implementation</option>
                            <option value="interactive" title="Interactive problem">interactive</option>
                            <option value="math" title="Mathematics including integration, differential equations, etc">math</option>
                            <option value="matrices" title="Matrix multiplication, determinant, Cramer's rule, systems of linear equations">matrices</option>
                            <option value="meet-in-the-middle" title="Meet-in-the-middle">meet-in-the-middle</option>
                            <option value="number theory" title="Number theory: Euler function, GCD, divisibility, etc">number theory</option>
                            <option value="probabilities" title="Probabilities, expected values, statistics, random variables, etc">probabilities</option>
                            <option value="schedules" title="Scheduling Algorithms">schedules</option>
                            <option value="shortest paths" title="Shortest paths on weighted and unweighted graphs">shortest paths</option>
                            <option value="sortings" title="Sortings, orderings">sortings</option>
                            <option value="string suffix structures" title="Suffix arrays, suffix trees, suffix automatas, etc">string suffix structures</option>
                            <option value="strings" title="Prefix- and Z-functions, suffix structures, Knuth–Morris–Pratt algorithm, etc">strings</option>
                            <option value="ternary search" title="Ternary search">ternary search</option>
                            <option value="trees" title="Trees">trees</option>
                            <option value="two pointers" title="Two pointers">two pointers</option>
                    </select><br><br>
                    
                    <button type="button" class="createweek">Create</button>
                </form>
            </div>
        </div>
    `;
    
    const sidebar = document.getElementById("sidebar");
    if (!sidebar) {
        console.error('Sidebar not found');
        return;
    }
    
    console.log('Inserting form into sidebar');
    
    if (!tableContainer) {
        sidebar.prepend(formContainer);
    } else {
        tableContainer.style.display = "none";
        sidebar.insertBefore(formContainer, tableContainer.nextSibling);
    }
    
    const createWeekButton = formContainer.querySelector(".createweek");
    if (createWeekButton) {
        //console.log('Adding click event listener to create week button');
        createWeekButton.addEventListener("click", createNewWeek);
    }
}

async function createNewWeek() {
    const handle = document.getElementById("handle").value;
    const key = document.getElementById("key").value;
    const secret = document.getElementById("secret").value;
    const tag = document.getElementById("topic").value;

    try {
        // First check if chrome.runtime is still available
        if (!chrome.runtime) {
            throw new Error('Extension context invalid');
        }

        const response = await chrome.runtime.sendMessage({
            type: 'fetchWeekData'
        });

        if (response.success && response.data && response.data.length > 0) {
            const currentWeek = response.data;
            const isCurrentWeekSolved = !currentWeek.some(question => question.status === "unsolved");

            if (!isCurrentWeekSolved) {
                alert("Current week is not solved yet, please solve all the questions before creating a new week.");
                refreshTable();
                return;
            }
        }

        localStorage.setItem("handle", handle);
        localStorage.setItem("key", key);
        localStorage.setItem("secret", secret);

        const createResponse = await chrome.runtime.sendMessage({
            type: 'createNewWeek',
            data: { handle, key, secret, tag }
        });

        if (createResponse.success) {
            refreshTable();
            return createResponse;
        } else {
            throw new Error(createResponse.error || 'Failed to create new week');
        }

    } catch (error) {
        if (error.message === 'Extension context invalid') {
            alert('The extension needs to be reloaded. Please refresh the page and try again.');
            window.location.reload();
        } else {
            console.error('Error creating new week:', error);
            alert('Failed to create new week. Please try again.');
        }
    }
}

function validateUrl(url) {
    const urlPattern = /^https:\/\/codeforces\.com\/contest\/\d+\/submission\/\d+$/;
    return urlPattern.test(url);
}


const observer = new MutationObserver((mutations) => {
   
    if (isProcessingInsertion) {
        return;
    }

   
    if (window.location.pathname.includes("/")) {
        const sidebar = document.getElementById("sidebar");
        if (sidebar && !document.getElementById("daily-question-extension")) {
            handle = localStorage.getItem("handle");
            key = localStorage.getItem("key");
            secret = localStorage.getItem("secret");
            
            isProcessingInsertion = true;
            runTask();

           
        }
    }
});

function runTask() {
    const intervalId = setInterval(() => {
        if (!checker) {
            console.log("Running insertDailyQuestionTable...");
            insertDailyQuestionTable().finally(() => {
                isProcessingInsertion = false;
                checker = true; 
            });
        } else {
            console.log("Stopping execution...");
            clearInterval(intervalId);
        }
    }, 2000); 
}


function startObserver() {
    const targetNode = document.body;
    observer.observe(targetNode, {
        childList: true,
        subtree: true,
        attributes: false,
        characterData: false
    });
}


startObserver();


window.addEventListener('unload', () => {
    observer.disconnect();
});

function removeExistingTable() {
    const existingTable = document.getElementById("daily-question-extension");
    if (existingTable) {
        existingTable.remove();
        hasTableBeenInserted = false; // Reset the flag when removing the table
    }
    
    // Also remove any existing forms
    const existingForm = document.getElementById("daily-question-form");
    if (existingForm) {
        existingForm.remove();
    }
}

async function refreshTable() {
    // Reset all state variables
    // hasTableBeenInserted = false;
    // weekData = null;
    // isProcessingInsertion = false;
    
    // // Remove existing table
    // removeExistingTable();
    // setTimeout(async () => {
    //     console.log('Refreshing table');
    //    startObserver();
    // }, 2000);

    //reload the page
    window.location.reload();
    
    // Wait for the table to be inserted
    
}
