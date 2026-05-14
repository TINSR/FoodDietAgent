
        const API_BASE = 'http://localhost:8080/api';
        let currentUserId = null;
        let currentFoodId = null;
        let agentData = null;
        let agentCache = { data: null, time: 0 };
        const AGENT_CACHE_MINUTES = 30;

        // Tab switching
        document.querySelectorAll('.tab').forEach(tab => {
            tab.addEventListener('click', () => {
                document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
                document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
                tab.classList.add('active');
                document.getElementById('page-' + tab.dataset.tab).classList.add('active');

                if (tab.dataset.tab === 'home') {
                    renderDatePicker();
                    loadTodaySummary(currentSelectedDate);
                }
                if (tab.dataset.tab === 'ai') {
                    loadAiPage();
                }
                if (tab.dataset.tab === 'add') {
                    loadFoods();
                }
                if (tab.dataset.tab === 'profile') loadProfile();
            });
        });

        // Date picker state
        let currentSelectedDate = new Date().toISOString().split('T')[0];

        function renderDatePicker() {
            const container = document.getElementById('date-picker');
            if (!container) return;
            const today = new Date();
            const days = [];
            for (let i = 6; i >= 0; i--) {
                const d = new Date(today);
                d.setDate(d.getDate() - i);
                days.push(d);
            }
            let html = '';
            days.forEach(d => {
                const iso = d.toISOString().split('T')[0];
                const dow = ['周日','周一','周二','周三','周四','周五','周六'][d.getDay()];
                const day = d.getDate();
                const isSelected = iso === currentSelectedDate;
                const isToday = iso === new Date().toISOString().split('T')[0];
                html += `<div class="date-chip" data-date="${iso}" onclick="selectDate('${iso}')" style="flex-shrink:0;padding:6px 12px;border-radius:8px;font-size:13px;cursor:pointer;background:${isSelected?'#4CAF50':'#f5f5f5'};color:${isSelected?'#fff':'#333'};border:1px solid ${isToday&&!isSelected?'#4CAF50':'transparent'};">${dow} ${day}</div>`;
            });
            container.innerHTML = html;
        }

        function selectDate(dateStr) {
            currentSelectedDate = dateStr;
            renderDatePicker();
            loadTodaySummary(dateStr);
        }

        // Load summary for a specific date
        async function loadTodaySummary(dateStr) {
            if (!currentUserId) return;
            const date = dateStr || new Date().toISOString().split('T')[0];
            try {
                const res = await fetch(`${API_BASE}/summary/${currentUserId}/${date}`);
                const json = await res.json();
                if (json.code === 200) {
                    agentData = json.data;
                    updateSummaryUI(json.data);
                }
            } catch (e) {
                console.log('No data yet');
            }
        }

        function updateSummaryUI(data) {
            // Calorie ring
            const consumed = data.consumedCalories || 0;
            const target = data.targetCalories || 2000;
            const progress = Math.min(100, (consumed / target) * 100);
            const remaining = Math.max(0, target - consumed);

            const ringHtml = `
                <div class="ring-inner">
                    <div class="ring-value">${consumed}</div>
                    <div class="ring-label">/ ${target} kcal</div>
                    <div style="font-size:11px;color:#666;margin-top:3px;">剩余 ${remaining} kcal</div>
                </div>
                <svg width="160" height="160" style="position:absolute;top:0;left:0;">
                    <circle cx="80" cy="80" r="75" fill="none" stroke="#eee" stroke-width="10"/>
                    <circle cx="80" cy="80" r="75" fill="none" stroke="${progress > 100 ? '#F44336' : '#4CAF50'}" stroke-width="10"
                        stroke-dasharray="${progress * 4.71} 471"
                        stroke-linecap="round" transform="rotate(-90 80 80)"/>
                </svg>
            `;
            document.getElementById('calorie-ring').innerHTML = ringHtml;

            // Status
            const statusEl = document.getElementById('status-text');
            statusEl.textContent = data.status || '正常';
            statusEl.className = 'ring-status ' + (data.status === '正常' || data.status === '已达标' ? 'normal' : data.status === '超标' ? 'danger' : 'warning');

            // Highlights
            const hlBar = document.getElementById('highlights-bar');
            if (data.highlights && data.highlights.length > 0) {
                hlBar.innerHTML = data.highlights.map(h =>
                    `<span style="background:#f0f0f0;padding:4px 10px;border-radius:12px;font-size:12px;color:#333;">${h}</span>`
                ).join('');
            } else {
                hlBar.innerHTML = '';
            }

            // Macros with real progress
            const carbTarget = data.carbsTarget || 200;
            const proteinTarget = data.proteinTarget || 150;
            const fatTarget = data.fatTarget || 65;

            document.getElementById('carb-val').textContent = (data.totalCarb || 0).toFixed(1);
            document.getElementById('protein-val').textContent = (data.totalProtein || 0).toFixed(1);
            document.getElementById('fat-val').textContent = (data.totalFat || 0).toFixed(1);

            document.getElementById('carb-target').textContent = carbTarget;
            document.getElementById('protein-target').textContent = proteinTarget;
            document.getElementById('fat-target').textContent = fatTarget;

            const carbP = data.carbsProgress || 0;
            const proteinP = data.proteinProgress || 0;
            const fatP = data.fatProgress || 0;

            document.getElementById('carb-bar').style.width = Math.min(100, carbP) + '%';
            document.getElementById('protein-bar').style.width = Math.min(100, proteinP) + '%';
            document.getElementById('fat-bar').style.width = Math.min(100, fatP) + '%';

            // AI Advice
            if (data.agentAdvice) {
                document.getElementById('advice-card').style.display = 'block';
                document.getElementById('advice-text').textContent = data.agentAdvice;
            } else {
                document.getElementById('advice-card').style.display = 'none';
            }

            // Meals
            let mealsHtml = '';
            if (data.meals && data.meals.length > 0) {
                data.meals.forEach(meal => {
                    if (meal.foods && meal.foods.length > 0) {
                    const mealClass = { '早餐': 'breakfast', '午餐': 'lunch', '晚餐': 'dinner', '加餐': 'snack' }[meal.mealType] || '';
                        mealsHtml += `
                            <div class="meal-section ${mealClass}">
                                <div class="meal-title">${meal.mealType} <span>${meal.calories || 0} kcal</span></div>
                                ${meal.foods.map(f => `
                                    <div class="food-item">
                                        <div>
                                            <div class="food-name">${f.foodName}</div>
                                            <div class="food-info">${f.weight || 0}g</div>
                                        </div>
                                        <div class="food-cal">${f.calories || 0} kcal</div>
                                    </div>
                                `).join('')}
                            </div>
                        `;
                    }
                });
            }
            document.getElementById('meals-container').innerHTML = mealsHtml;
        }

        function loadAiPage() {
            // Chat interface doesn't need prepopulation
        }

        // Send chat message
        async function sendChat(message) {
            if (!currentUserId) {
                addChatMessage('user', message);
                addChatMessage('ai', '请先在"我的"页面注册/登录后再使用AI分析功能。');
                return;
            }
            addChatMessage('user', message);
            addChatMessage('ai', '正在分析...');
            saveChatHistory();

            try {
                const res = await fetch(`${API_BASE}/agent/chat`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ userId: currentUserId, message: message })
                });
                const json = await res.json();
                const messagesDiv = document.getElementById('chat-messages');
                const lastMsg = messagesDiv.lastElementChild;
                if (lastMsg && lastMsg.textContent === '正在分析...') lastMsg.remove();
                if (json.code === 200 && json.data) {
                    let fullResponse = '';
                    if (json.data.advice) fullResponse += json.data.advice;
                    if (json.data.tips) fullResponse += '\n\n💡 ' + json.data.tips;
                    if (!fullResponse || fullResponse === '正在分析...') fullResponse = '分析完成';
                    addChatMessage('ai', fullResponse);
                    saveChatHistory();

                    if (json.data.recipes && json.data.recipes.length > 0) {
                        document.getElementById('ai-recipe-section').style.display = 'block';
                        let recipesHtml = '';
                        json.data.recipes.forEach(r => {
                            recipesHtml += `
                                <div class="recipe-card">
                                    <div class="recipe-name">${r.name || '未知食谱'}</div>
                                    ${r.description ? `<div class="recipe-desc">${r.description}</div>` : ''}
                                    <div class="recipe-calories">${r.calories || 0} kcal</div>
                                    <div class="recipe-macro">
                                        <span>蛋白质 ${r.protein || 0}g</span>
                                        <span>碳水 ${r.carbs || 0}g</span>
                                        <span>脂肪 ${r.fat || 0}g</span>
                                    </div>
                                    ${r.ingredients && r.ingredients.length > 0 ? `<div class="recipe-ingredients">配料：${r.ingredients.join('、')}</div>` : ''}
                                </div>
                            `;
                        });
                        document.getElementById('ai-recipes-container').innerHTML = recipesHtml;
                    }
                } else {
                    addChatMessage('ai', json.message || '分析失败，请稍后重试');
                    saveChatHistory();
                }
            } catch (e) {
                const messagesDiv = document.getElementById('chat-messages');
                const lastMsg = messagesDiv.lastElementChild;
                if (lastMsg && lastMsg.textContent === '正在分析...') lastMsg.remove();
                addChatMessage('ai', '网络错误，请稍后重试');
                saveChatHistory();
            }
        }

        function saveChatHistory() {
            const messagesDiv = document.getElementById('chat-messages');
            const msgs = [];
            messagesDiv.querySelectorAll('div').forEach(m => {
                const isUser = m.style.alignSelf === 'flex-end';
                msgs.push({ type: isUser ? 'user' : 'ai', text: m.dataset.original || m.innerText });
            });
            if (currentUserId) {
                localStorage.setItem('chat_' + currentUserId, JSON.stringify(msgs));
            }
        }

        function loadChatHistory() {
            if (!currentUserId) return;
            const saved = localStorage.getItem('chat_' + currentUserId);
            if (!saved) return;
            try {
                const msgs = JSON.parse(saved);
                const messagesDiv = document.getElementById('chat-messages');
                messagesDiv.innerHTML = '';
                msgs.forEach(m => addChatMessage(m.type, m.text));
            } catch (e) {}
        }

        function sendCustomChat() {
            const input = document.getElementById('chat-input');
            const msg = input.value.trim();
            if (!msg) return;
            input.value = '';
            sendChat(msg);
        }

        function addChatMessage(type, text) {
            const messagesDiv = document.getElementById('chat-messages');
            const msgDiv = document.createElement('div');
            msgDiv.dataset.original = text;
            // Handle literal \n (from LLM escaped newlines) and real newlines
            const backslashN = String.fromCharCode(92) + 'n';
            const processed = text.split(backslashN).join('<br>').replace(/\n/g, '<br>');
            const escaped = processed.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
            msgDiv.innerHTML = escaped;
            msgDiv.style.cssText = type === 'user'
                ? 'align-self:flex-end;background:#4CAF50;color:#fff;padding:10px 14px;border-radius:16px 16px 4px 16px;max-width:80%;font-size:14px;word-break:break-word;'
                : 'align-self:flex-start;background:#f1f1f1;color:#333;padding:10px 14px;border-radius:16px 16px 16px 4px;max-width:80%;font-size:14px;word-break:break-word;';
            messagesDiv.appendChild(msgDiv);
            messagesDiv.scrollTop = messagesDiv.scrollHeight;
        }

        function updateAgentUI(data) {
            // Update AI page advice
            if (data.advice) {
                document.getElementById('ai-advice-card').style.display = 'block';
                document.getElementById('ai-advice-text').textContent = data.advice;
                if (data.tips) {
                    document.getElementById('ai-advice-tip').style.display = 'block';
                    document.getElementById('ai-advice-tip').textContent = '💡 ' + data.tips;
                }
            }

            // Update AI page recipes
            if (data.recipes && data.recipes.length > 0) {
                document.getElementById('ai-recipe-section').style.display = 'block';
                let recipesHtml = '';
                data.recipes.forEach(r => {
                    recipesHtml += `
                        <div class="recipe-card">
                            <div class="recipe-name">${r.name || '未知食谱'}</div>
                            ${r.description ? `<div class="recipe-desc">${r.description}</div>` : ''}
                            <div class="recipe-calories">${r.calories || 0} kcal</div>
                            <div class="recipe-macro">
                                <span>蛋白质 ${r.protein || 0}g</span>
                                <span>碳水 ${r.carbs || 0}g</span>
                                <span>脂肪 ${r.fat || 0}g</span>
                            </div>
                            ${r.ingredients && r.ingredients.length > 0 ? `<div class="recipe-ingredients">配料：${r.ingredients.join('、')}</div>` : ''}
                        </div>
                    `;
                });
                document.getElementById('ai-recipes-container').innerHTML = recipesHtml;
            }
        }

        // Load foods for dropdown
        let allFoods = [];
        async function loadFoods() {
            try {
                const res = await fetch(`${API_BASE}/food/foods/all`);
                const json = await res.json();
                if (json.code === 200) {
                    allFoods = json.data;
                    let options = '<option value="">-- 请选择食物 --</option>';
                    json.data.forEach(food => {
                        options += `<option value="${food.id}">${food.name} (${food.caloriesPer100g}kcal/100g)</option>`;
                    });
                    document.getElementById('food-select').innerHTML = options;
                }
            } catch (e) {
                console.log('loadFoods failed:', e);
            }
        }

        // Local filter for food search (avoids Chinese encoding issues)
        function filterFoods(keyword) {
            if (!keyword) return allFoods.slice(0, 20);
            const lower = keyword.toLowerCase();
            return allFoods.filter(f => f.name.toLowerCase().includes(lower)).slice(0, 20);
        }

        // Add food
        async function addFood() {
            const foodId = document.getElementById('food-select').value;
            const weight = document.getElementById('food-weight').value;
            const mealType = document.getElementById('meal-type').value;

            if (!foodId) {
                alert('请选择食物');
                return;
            }

            try {
                const res = await fetch(`${API_BASE}/food/log`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        userId: currentUserId,
                        foodId: parseInt(foodId),
                        weight: parseInt(weight),
                        mealType: mealType
                    })
                });
                const json = await res.json();
                if (json.code === 200) {
                    alert('添加成功');
                    document.querySelector('[data-tab="home"]').click();
                }
            } catch (e) {
                alert('添加失败');
            }
        }

        // Search food
        async function searchFood() {
            const keyword = document.getElementById('search-keyword').value;
            // Use local filter to avoid Chinese encoding issues with backend
            const results = filterFoods(keyword);
            renderSearchResults(results);
        }

        function renderSearchResults(foods) {
            let html = '';
            if (foods && foods.length > 0) {
                foods.forEach(food => {
                    html += `
                        <div class="food-list-item" onclick="selectFood(${food.id}, '${food.name}', ${food.caloriesPer100g}, ${food.proteinPer100g||0}, ${food.carbsPer100g||0}, ${food.fatPer100g||0})">
                            <div>
                                <div class="food-list-name">${food.name}</div>
                                <div class="food-list-info">${food.category || ''} · 蛋白质${food.proteinPer100g || 0}g · 碳水${food.carbsPer100g || 0}g</div>
                            </div>
                            <div class="food-list-cal">${food.caloriesPer100g || 0} kcal</div>
                        </div>
                    `;
                });
            } else {
                html = '<div style="padding:20px;text-align:center;color:#999;">没有找到相关食物</div>';
            }
            document.getElementById('search-results').innerHTML = html;
        }

        function switchFoodTab(tab) {
            document.getElementById('add-system-section').style.display = tab === 'system' ? 'block' : 'none';
            document.getElementById('add-my-section').style.display = tab === 'my' ? 'block' : 'none';
            document.getElementById('tab-system-foods').style.background = tab === 'system' ? '#4CAF50' : '#999';
            document.getElementById('tab-my-foods').style.background = tab === 'my' ? '#8BC34A' : '#999';
            if (tab === 'my') loadMyFoodsForAdd();
        }

        async function loadMyFoodsForAdd() {
            if (!currentUserId) return;
            const container = document.getElementById('my-foods-list');
            try {
                const res = await fetch(`${API_BASE}/user-foods/${currentUserId}`);
                const json = await res.json();
                if (json.code === 200) {
                    const list = json.data || [];
                    if (!list.length) {
                        container.innerHTML = '<div style="padding:20px;text-align:center;color:#999;">还没有自定义食物，去"我的"页面创建吧</div>';
                        return;
                    }
                    container.innerHTML = list.map(f => `
                        <div class="food-list-item" onclick="selectUserFood(${f.id}, '${f.name}', ${f.totalCalories}, ${f.carbs||0}, ${f.protein||0}, ${f.fat||0})">
                            <div>
                                <div class="food-list-name">⭐ ${f.name}</div>
                                <div class="food-list-info">${f.cookingMethod || ''} · 碳水${(f.carbs||0).toFixed(1)}g · 蛋白质${(f.protein||0).toFixed(1)}g</div>
                            </div>
                            <div class="food-list-cal">${f.totalCalories} kcal</div>
                        </div>
                    `).join('');
                }
            } catch (e) { container.innerHTML = '<div style="padding:20px;color:#999;">加载失败</div>'; }
        }

        let pendingFoods = [];

        function selectFood(foodId, name, cal, protein, carbs, fat) {
            pendingFoods.push({ foodId, name, cal, protein, carbs, fat, weight: 100, mealType: '午餐' });
            renderPendingList();
        }

        function selectUserFood(userFoodId, name, cal, carbs, protein, fat) {
            pendingFoods.push({ userFoodId, name, cal, protein, carbs, fat, weight: 100, mealType: '午餐' });
            renderPendingList();
        }

        function renderPendingList() {
            const container = document.getElementById('pending-add-list');
            const itemsDiv = document.getElementById('pending-items');
            if (!pendingFoods.length) {
                container.style.display = 'none';
                return;
            }
            container.style.display = 'block';
            let html = '';
            pendingFoods.forEach((item, idx) => {
                html += `
                    <div style="display:flex;align-items:center;gap:8px;padding:8px 0;border-bottom:1px solid #eee;">
                        <div style="flex:1;min-width:0;">
                            <div style="font-size:13px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${item.name}</div>
                            <div style="font-size:11px;color:#999;">${item.cal}kcal/100g · 蛋白质${item.protein||0}g</div>
                        </div>
                        <input type="number" value="${item.weight}" min="1" style="width:60px;text-align:center;border:1px solid #ddd;border-radius:4px;padding:4px;" onchange="changePendingWeight(${idx}, this.value)">
                        <select style="border:1px solid #ddd;border-radius:4px;padding:4px;" onchange="changePendingMeal(${idx}, this.value)">
                            <option value="早餐" ${item.mealType==='早餐'?'selected':''}>早餐</option>
                            <option value="午餐" ${item.mealType==='午餐'?'selected':''}>午餐</option>
                            <option value="晚餐" ${item.mealType==='晚餐'?'selected':''}>晚餐</option>
                            <option value="加餐" ${item.mealType==='加餐'?'selected':''}>加餐</option>
                        </select>
                        <button onclick="removePendingItem(${idx})" style="background:none;border:none;color:#999;font-size:16px;cursor:pointer;padding:4px;">✕</button>
                    </div>
                `;
            });
            itemsDiv.innerHTML = html;
        }

        function changePendingWeight(idx, weight) {
            pendingFoods[idx].weight = parseInt(weight) || 100;
        }

        function changePendingMeal(idx, meal) {
            pendingFoods[idx].mealType = meal;
        }

        function removePendingItem(idx) {
            pendingFoods.splice(idx, 1);
            renderPendingList();
        }

        async function confirmBatchAdd() {
            if (!pendingFoods.length || !currentUserId) return;
            let success = 0, failed = 0;
            for (const item of pendingFoods) {
                try {
                    const payload = { userId: currentUserId, weight: item.weight, mealType: item.mealType };
                    if (item.userFoodId) {
                        payload.userFoodId = item.userFoodId;
                    } else {
                        payload.foodId = item.foodId;
                    }
                    const res = await fetch(`${API_BASE}/food/log`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(payload)
                    });
                    if (res.ok) success++;
                    else failed++;
                } catch (e) { failed++; }
            }
            alert(`添加完成：成功 ${success} 条${failed > 0 ? '，失败 ' + failed + ' 条' : ''}`);
            pendingFoods = [];
            renderPendingList();
            document.querySelector('[data-tab="home"]').click();
        }

        function closeModal() {
            document.getElementById('add-modal').classList.remove('active');
        }

        async function confirmAddFood() {
            const weight = document.getElementById('modal-weight').value;
            const mealType = document.getElementById('modal-meal-type').value;

            try {
                const res = await fetch(`${API_BASE}/food/log`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        userId: currentUserId,
                        foodId: currentFoodId,
                        weight: parseInt(weight),
                        mealType: mealType
                    })
                });
                const json = await res.json();
                if (json.code === 200) {
                    alert('添加成功');
                    closeModal();
                    document.querySelector('[data-tab="home"]').click();
                }
            } catch (e) {
                alert('添加失败');
            }
        }

        // Load profile
        async function loadProfile() {
            if (!currentUserId) {
                document.getElementById('profile-name').textContent = '未注册';
                document.getElementById('profile-info').textContent = '请先注册';
                document.getElementById('profile-target').textContent = '0';
                return;
            }
            try {
                const res = await fetch(`${API_BASE}/user/${currentUserId}`);
                const json = await res.json();
                if (json.code === 200) {
                    const d = json.data;
                    document.getElementById('profile-name').textContent = d.name;
                    document.getElementById('profile-info').textContent = `${d.age}岁 · ${d.gender} · ${d.height}cm · ${d.weight}kg · ${d.goal}`;
                    document.getElementById('profile-target').textContent = d.dailyCalorieTarget;
                    document.getElementById('profile-btn').textContent = '更新信息';

                    // Load macro target
                    try {
                        const macroRes = await fetch(`${API_BASE}/macro-target/${currentUserId}`);
                        const macroJson = await macroRes.json();
                        if (macroJson.code === 200 && macroJson.data) {
                            document.getElementById('profile-macros').style.display = 'flex';
                            document.getElementById('profile-protein').textContent = macroJson.data.proteinGrams || 0;
                            document.getElementById('profile-carbs').textContent = macroJson.data.carbsGrams || 0;
                            document.getElementById('profile-fat').textContent = macroJson.data.fatGrams || 0;
                        }
                    } catch (e) {}

                    // Fill form
                    document.getElementById('reg-name').value = d.name || '';
                    document.getElementById('reg-age').value = d.age || '';
                    document.getElementById('reg-gender').value = d.gender || '男';
                    document.getElementById('reg-height').value = d.height || '';
                    document.getElementById('reg-weight').value = d.weight || '';
                    document.getElementById('reg-goal').value = d.goal || '维持';
                    document.getElementById('reg-activity').value = d.activityLevel || '久坐';
                    loadWeeklyChart();
                }
            } catch (e) {
                console.log(e);
            }
        }

        async function showUserFoodLibrary() {
            if (!currentUserId) { alert('请先登录'); return; }
            const container = document.getElementById('user-food-library');
            const isVisible = container.style.display !== 'none';
            if (isVisible) {
                container.style.display = 'none';
                return;
            }
            container.style.display = 'block';
            await loadUserFoodList();
        }

        async function loadUserFoodList() {
            const container = document.getElementById('user-food-library');
            try {
                const res = await fetch(`${API_BASE}/user-foods/${currentUserId}`);
                const json = await res.json();
                if (json.code === 200) {
                    const list = json.data || [];
                    if (!list.length) {
                        container.innerHTML = `
                            <div style="text-align:center;color:#999;padding:20px;">
                                <div style="font-size:14px;margin-bottom:10px;">还没有自定义食物</div>
                                <button class="form-btn" style="width:100%;" onclick="showCreateUserFood()">➕ 创建我的食物</button>
                            </div>`;
                        return;
                    }
                    container.innerHTML = `
                        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px;">
                            <span style="font-size:14px;font-weight:600;">🍳 我的食物（共${list.length}个）</span>
                            <button class="form-btn" style="padding:6px 12px;font-size:12px;" onclick="showCreateUserFood()">➕ 新建</button>
                        </div>
                        <div id="user-food-list">${list.map(f => `
                            <div style="background:#f9f9f9;border-radius:8px;padding:10px;margin-bottom:8px;display:flex;justify-content:space-between;align-items:center;">
                                <div>
                                    <div style="font-size:13px;font-weight:600;">${f.name}</div>
                                    <div style="font-size:11px;color:#666;">${f.cookingMethod || ''} · ${f.totalCalories}kcal</div>
                                </div>
                                <button onclick="deleteUserFood(${f.id})" style="background:none;border:none;color:#999;font-size:14px;cursor:pointer;">✕</button>
                            </div>
                        `).join('')}</div>`;
                }
            } catch (e) { container.innerHTML = '<div style="color:#999;text-align:center;padding:20px;">加载失败</div>'; }
        }

        async function showCreateUserFood() {
            const container = document.getElementById('user-food-library');
            container.innerHTML = `
                <div style="font-size:14px;font-weight:600;margin-bottom:10px;">创建自定义食物</div>
                <div class="form-group"><label class="form-label">食物名称</label><input type="text" class="form-input" id="uf-name" placeholder="如：番茄炒蛋"></div>
                <div class="form-group"><label class="form-label">烹饪方式</label>
                    <select class="form-select" id="uf-method">
                        <option value="炒">炒</option><option value="煮">煮</option><option value="蒸">蒸</option>
                        <option value="炖">炖</option><option value="炸">炸</option><option value="生吃">生吃</option>
                    </select>
                </div>
                <div id="uf-ingredients"></div>
                <button class="form-btn" style="width:100%;margin-top:8px;" onclick="addUfIngredientRow()">➕ 加食材</button>
                <div id="uf-seasonings"></div>
                <button class="form-btn" style="width:100%;margin-top:8px;background:#8BC34A;" onclick="addUfSeasoningRow()">➕ 加调味料</button>
                <div id="uf-preview" style="background:#E8F5E9;border-radius:8px;padding:10px;margin-top:10px;text-align:center;">
                    <div style="font-size:13px;color:#4CAF50;font-weight:600;">总热量：<span id="uf-total-cal">0</span> kcal</div>
                </div>
                <button class="form-btn" style="width:100%;margin-top:10px;" onclick="saveUserFood()">保存</button>
            `;
            // Init with one empty ingredient row
            addUfIngredientRow();
        }

        async function addUfIngredientRow() {
            const container = document.getElementById('uf-ingredients');
            await loadFoodsForUf();
            const rows = container.querySelectorAll('.uf-ing-row');
            const idx = rows.length;
            const div = document.createElement('div');
            div.className = 'uf-ing-row';
            div.style = 'display:flex;gap:4px;margin-bottom:4px;';
            div.innerHTML = `
                <select class="form-select" id="uf-ing-${idx}" style="flex:2;" onchange="calcUfCalories()">
                    <option value="">-- 食材 --</option>
                    ${allFoods.map(f => `<option value="${f.id}">${f.name}</option>`).join('')}
                </select>
                <input type="number" class="form-input" id="uf-ing-w-${idx}" value="100" style="flex:1;" placeholder="克" oninput="calcUfCalories()">
                <button onclick="this.parentElement.remove();calcUfCalories()" style="background:none;border:none;color:#999;font-size:14px;">✕</button>
            `;
            container.appendChild(div);
        }

        async function addUfSeasoningRow() {
            const SEASONINGS = [['食用油','油',9],['酱油','',0.74],['盐','',0],['醋','',0.13],['糖','',4],['蚝油','',0.78],['芝麻油','',8.9],['辣椒油','',8.6],['番茄酱','',1]];
            const container = document.getElementById('uf-seasonings');
            const idx = container.querySelectorAll('.uf-sea-row').length;
            const div = document.createElement('div');
            div.className = 'uf-sea-row';
            div.style = 'display:flex;gap:4px;margin-bottom:4px;';
            div.innerHTML = `
                <select class="form-select" id="uf-sea-${idx}" style="flex:2;" onchange="calcUfCalories()">
                    <option value="">-- 调味料 --</option>
                    ${SEASONINGS.map(([name,,]) => `<option value="${name}">${name}</option>`).join('')}
                </select>
                <input type="number" class="form-input" id="uf-sea-w-${idx}" value="5" style="flex:1;" placeholder="克" oninput="calcUfCalories()">
                <button onclick="this.parentElement.remove();calcUfCalories()" style="background:none;border:none;color:#999;font-size:14px;">✕</button>
            `;
            container.appendChild(div);
        }

        async function calcUfCalories() {
            // Live preview - just show 0 for now, real calc done server-side
            let total = 0;
            const ingRows = document.querySelectorAll('.uf-ing-row');
            for (const row of ingRows) {
                const select = row.querySelector('select');
                const weightInput = row.querySelector('input[type=number]');
                if (!select.value) continue;
                const food = allFoods.find(f => f.id == select.value);
                if (food) {
                    const w = parseInt(weightInput.value) || 0;
                    total += w * (food.caloriesPer100g || 0) / 100;
                }
            }
            document.getElementById('uf-total-cal').textContent = Math.round(total);
        }

        async function saveUserFood() {
            const name = document.getElementById('uf-name').value.trim();
            const method = document.getElementById('uf-method').value;
            if (!name) { alert('请输入食物名称'); return; }

            const ingredients = [];
            document.querySelectorAll('.uf-ing-row').forEach(row => {
                const select = row.querySelector('select');
                const weightInput = row.querySelector('input[type=number]');
                if (select.value) {
                    ingredients.push({
                        foodId: parseInt(select.value),
                        ingredientName: select.options[select.selectedIndex].text,
                        grams: parseInt(weightInput.value) || 100,
                        isSeasoning: false
                    });
                }
            });
            document.querySelectorAll('.uf-sea-row').forEach(row => {
                const select = row.querySelector('select');
                const weightInput = row.querySelector('input[type=number]');
                if (select.value) {
                    ingredients.push({
                        ingredientName: select.value,
                        grams: parseInt(weightInput.value) || 5,
                        isSeasoning: true
                    });
                }
            });

            try {
                const res = await fetch(`${API_BASE}/user-foods`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ userId: currentUserId, name, cookingMethod: method, ingredients })
                });
                const json = await res.json();
                if (json.code === 200) {
                    alert('保存成功！');
                    await loadUserFoodList();
                } else {
                    alert('保存失败：' + json.message);
                }
            } catch (e) { alert('保存失败'); }
        }

        async function deleteUserFood(id) {
            if (!confirm('确定删除？')) return;
            try {
                await fetch(`${API_BASE}/user-foods/${id}`, { method: 'DELETE' });
                await loadUserFoodList();
            } catch (e) { alert('删除失败'); }
        }

        async function loadFoodsForUf() {
            if (allFoods.length) return;
            const res = await fetch(`${API_BASE}/food/foods/all`);
            const json = await res.json();
            if (json.code === 200) allFoods = json.data;
        }

        async function loadWeeklyChart() {
            if (!currentUserId) return;
            try {
                const res = await fetch(`${API_BASE}/summary/${currentUserId}/weekly?days=7`);
                const json = await res.json();
                if (json.code === 200 && json.data) {
                    renderWeeklyChart(json.data);
                }
            } catch (e) {}
        }

        function renderWeeklyChart(data) {
            const container = document.getElementById('weekly-chart');
            if (!container || !data || data.length === 0) return;

            const maxCal = Math.max(...data.map(d => d.consumedCalories || 0), 1);
            const today = new Date().toISOString().split('T')[0];

            container.innerHTML = data.map((d, i) => {
                const pct = d.consumedCalories / maxCal * 100;
                const isTarget = d.consumedCalories >= d.targetCalories * 0.9 && d.consumedCalories <= d.targetCalories * 1.1;
                const color = d.consumedCalories > d.targetCalories ? '#F44336' : isTarget ? '#4CAF50' : '#FF9800';
                const date = d.date ? d.date.split('-')[2] : '';
                return `
                    <div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-flex-end;height:100%;gap:4px;">
                        <div style="width:100%;display:flex;align-items:flex-end;justify-content:center;height:80%;">
                            <div style="width:80%;background:${color};border-radius:4px 4px 0 0;height:${pct}%;min-height:${pct > 0 ? 4 : 0}px;" title="${d.consumedCalories}kcal"></div>
                        </div>
                        <div style="font-size:10px;color:#999;">${date}</div>
                    </div>
                `;
            }).join('');
        }

        function showProfileForm() {
            const form = document.getElementById('profile-form');
            form.style.display = form.style.display === 'none' ? 'block' : 'none';
        }

        async function registerUser() {
            const data = {
                name: document.getElementById('reg-name').value,
                age: parseInt(document.getElementById('reg-age').value),
                gender: document.getElementById('reg-gender').value,
                height: parseFloat(document.getElementById('reg-height').value),
                weight: parseFloat(document.getElementById('reg-weight').value),
                goal: document.getElementById('reg-goal').value,
                activityLevel: document.getElementById('reg-activity').value
            };

            try {
                const res = await fetch(`${API_BASE}/user/register`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });
                const json = await res.json();
                if (json.code === 200) {
                    currentUserId = json.data.id;
                    localStorage.setItem('userId', currentUserId);
                    alert('注册成功');
                    document.getElementById('profile-form').style.display = 'none';
                    loadProfile();
                    loadTodaySummary();
                }
            } catch (e) {
                alert('注册失败');
            }
        }

        // Init
        function init() {
            const savedUserId = localStorage.getItem('userId');
            if (savedUserId) {
                currentUserId = parseInt(savedUserId);
            }
            renderDatePicker();
            loadTodaySummary(currentSelectedDate);
            if (currentUserId) {
                loadAgentRecommendation();
            }
        }

        init();
    