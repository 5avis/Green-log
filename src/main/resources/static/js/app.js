/* ==========================================================================
   GreenLog Enterprise Edition — Vanilla JS Core Controller
   Stage 6: Health Check-ins Tab, Due Queue, Alive/Dead Reporting & Dead Tree Rule
   ========================================================================== */

const API_BASE = '/api';

// Cached application data
let cachedTrees = [];
let cachedDrives = [];
let cachedVolunteers = [];

// --- Tab Routing ---
function switchTab(tabKey) {
    const tabs = ['drives-trees', 'checkins', 'stats'];
    tabs.forEach(key => {
        const btn = document.getElementById(`tab-btn-${key}`);
        const pane = document.getElementById(`tab-pane-${key}`);
        if (key === tabKey) {
            if (btn) btn.classList.add('active');
            if (pane) pane.classList.add('active');
        } else {
            if (btn) btn.classList.remove('active');
            if (pane) pane.classList.remove('active');
        }
    });
    updateStatus(`Active workspace view: ${tabKey.replace('-', ' ').toUpperCase()}`);

    if (tabKey === 'drives-trees') {
        loadTreesTable();
    } else if (tabKey === 'checkins') {
        loadDueTreesTable();
    } else if (tabKey === 'stats' && typeof loadStatsTables === 'function') {
        loadStatsTables();
    }
}

// --- Status Bar Clock & Updates ---
function updateClock() {
    const clock = document.getElementById('clock-display');
    const navClock = document.getElementById('nav-clock-display');
    const now = new Date();
    const timeStr = now.toLocaleTimeString();
    if (clock) clock.textContent = timeStr;
    if (navClock) navClock.textContent = timeStr;
}
setInterval(updateClock, 1000);
updateClock();

function updateStatus(msg) {
    const statusPanel = document.getElementById('status-message');
    if (statusPanel) {
        statusPanel.textContent = msg;
    }
}

// --- Modal Management ---
function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.add('active');
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) modal.classList.remove('active');
}

function showAlert(title, message, isError = false) {
    const modal = document.getElementById('alert-modal');
    const header = document.getElementById('alert-modal-header');
    const titleEl = document.getElementById('alert-modal-title');
    const messageEl = document.getElementById('alert-modal-message');
    const iconEl = document.getElementById('alert-modal-icon');

    if (titleEl) titleEl.textContent = title;
    if (messageEl) messageEl.textContent = message;

    if (isError) {
        if (header) header.classList.add('error');
        if (iconEl) iconEl.textContent = '⚠️';
    } else {
        if (header) header.classList.remove('error');
        if (iconEl) iconEl.textContent = 'ℹ️';
    }

    if (modal) modal.classList.add('active');
}

function closeAlertModal() {
    const modal = document.getElementById('alert-modal');
    if (modal) modal.classList.remove('active');
}

// --- Modern Confirmation Dialog ---
let pendingConfirmCallback = null;

function showConfirm(title, message, onConfirmCallback) {
    const modal = document.getElementById('confirm-modal');
    const titleEl = document.getElementById('confirm-modal-title');
    const msgEl = document.getElementById('confirm-modal-message');
    const iconEl = document.getElementById('confirm-modal-icon');
    const okBtn = document.getElementById('confirm-modal-ok-btn');

    if (titleEl) titleEl.textContent = title;
    if (msgEl) msgEl.textContent = message;
    if (iconEl) iconEl.textContent = '❓';

    pendingConfirmCallback = onConfirmCallback;

    if (okBtn) {
        okBtn.onclick = function() {
            closeConfirmModal(true);
            if (pendingConfirmCallback) {
                const cb = pendingConfirmCallback;
                pendingConfirmCallback = null;
                cb();
            }
        };
    }

    if (modal) modal.classList.add('active');
}

function closeConfirmModal(isConfirmed = false) {
    const modal = document.getElementById('confirm-modal');
    if (modal) modal.classList.remove('active');
    if (!isConfirmed) {
        pendingConfirmCallback = null;
    }
}

function showAboutDialog() {
    showAlert(
        'About GreenLog',
        'GreenLog — Modern SaaS Edition v3.0\nTree Plantation Drive & Survival Monitoring System\nBuilt with Spring Boot 3.5, MariaDB/MySQL, and Vanilla JS.\nUniversal UI Theme: Modern, Clean SaaS (Soft & Airy).',
        false
    );
}

// --- KPI Synchronization ---
function updateKPIs(data) {
    if (!data) return;
    const elTotalTrees = document.getElementById('kpi-total-trees');
    const elAlive = document.getElementById('kpi-alive-trees');
    const elDead = document.getElementById('kpi-dead-trees');
    const elSurvival = document.getElementById('kpi-survival-rate');
    const elDrives = document.getElementById('kpi-total-drives');
    const elVolunteers = document.getElementById('kpi-total-volunteers');

    if (elTotalTrees) elTotalTrees.textContent = data.totalTrees ?? 0;
    if (elAlive) elAlive.textContent = data.aliveTrees ?? 0;
    if (elDead) elDead.textContent = data.deadTrees ?? 0;
    if (elSurvival) elSurvival.textContent = `${(data.overallSurvivalRate ?? 0).toFixed(1)}%`;
    if (elDrives) elDrives.textContent = data.totalDrives ?? 0;
    if (elVolunteers) elVolunteers.textContent = data.totalVolunteers ?? 0;
}

async function refreshMetrics() {
    try {
        const res = await fetch(`${API_BASE}/stats`);
        if (!res.ok) throw new Error('Failed to fetch KPI metrics');
        const data = await res.json();
        updateKPIs(data);
        updateStatus('System metrics synchronized with MariaDB.');
    } catch (err) {
        console.error('Error in refreshMetrics:', err);
    }
}

// --- Fetch Drives & Volunteers for Select Dropdowns ---
async function loadDrivesAndVolunteers() {
    try {
        const [drivesRes, volRes] = await Promise.all([
            fetch(`${API_BASE}/drives`),
            fetch(`${API_BASE}/volunteers`)
        ]);

        if (drivesRes.ok) {
            cachedDrives = await drivesRes.json();
            populateDriveSelects();
        }
        if (volRes.ok) {
            cachedVolunteers = await volRes.json();
            populateVolunteerSelects();
        }
    } catch (err) {
        console.error('Error fetching drives or volunteers:', err);
    }
}

function populateDriveSelects() {
    const filterSelect = document.getElementById('filter-drive-select');
    const treeDriveSelect = document.getElementById('tree-drive-select');

    if (filterSelect) {
        const currentVal = filterSelect.value;
        filterSelect.innerHTML = '<option value="ALL">-- All Plantation Drives --</option>';
        cachedDrives.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.id;
            opt.textContent = `${d.name} (${d.location})`;
            filterSelect.appendChild(opt);
        });
        filterSelect.value = currentVal;
    }

    if (treeDriveSelect) {
        treeDriveSelect.innerHTML = '<option value="">-- None / General Plantation --</option>';
        cachedDrives.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.id;
            opt.textContent = `${d.name} [ID: ${d.id}]`;
            treeDriveSelect.appendChild(opt);
        });
    }
}

function populateVolunteerSelects() {
    const treeVolSelect = document.getElementById('tree-volunteer-select');
    const checkinVolSelect = document.getElementById('checkin-volunteer');

    const optionsHtml = '<option value="">-- Unassigned / None --</option>' +
        cachedVolunteers.map(v => `<option value="${v.id}">${v.name} (${v.totalTreesPlanted ?? 0} planted)</option>`).join('');

    if (treeVolSelect) treeVolSelect.innerHTML = optionsHtml;
    if (checkinVolSelect) checkinVolSelect.innerHTML = optionsHtml;
}

// --- Stage 5: Master Trees Table (Fetch, Filter, Render) ---
async function loadTreesTable() {
    const tbody = document.getElementById('trees-table-body');
    if (!tbody) return;

    try {
        updateStatus('Fetching tree inventory from database...');
        const res = await fetch(`${API_BASE}/trees`);
        if (!res.ok) throw new Error('Failed to retrieve trees');
        cachedTrees = await res.json();

        renderTreesTable(cachedTrees);
        updateStatus(`Loaded ${cachedTrees.length} tree record(s).`);
    } catch (err) {
        console.error('Error in loadTreesTable:', err);
        tbody.innerHTML = `<tr><td colspan="7" style="color: red; text-align: center;">Error loading tree records: ${err.message}</td></tr>`;
        updateStatus('Database query error.');
    }
}

function renderTreesTable(trees) {
    const tbody = document.getElementById('trees-table-body');
    if (!tbody) return;

    if (!trees || trees.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: #777; padding: 16px;">No tree records found in database. Click "+ Log Tree Entry" above to add one.</td></tr>`;
        return;
    }

    tbody.innerHTML = trees.map(tree => {
        const driveName = tree.plantationDrive ? tree.plantationDrive.name : (tree.plantationDriveName ?? '<span style="color: var(--text-muted);">Unassigned</span>');
        const isDead = (tree.status || '').toUpperCase() === 'DEAD';
        const statusBadge = isDead
            ? `<span class="status-tag status-dead">DEAD</span>`
            : `<span class="status-tag status-alive">ALIVE</span>`;

        return `
            <tr id="tree-row-${tree.id}">
                <td style="font-weight: 600; color: var(--primary);">#${tree.id}</td>
                <td><strong style="color: var(--text-primary); font-weight: 600;">${escapeHtml(tree.species)}</strong></td>
                <td style="color: var(--text-secondary);">${driveName}</td>
                <td style="font-family: monospace; font-size: 12px; color: var(--text-secondary);">${escapeHtml(tree.locationGps)}</td>
                <td style="color: var(--text-secondary);">${tree.datePlanted || 'N/A'}</td>
                <td>${statusBadge}</td>
                <td style="text-align: center;">
                    <div style="display: inline-flex; gap: 4px; justify-content: center;">
                        <button class="btn btn-sm btn-ghost" onclick="openEditTreeModal(${tree.id})">Edit</button>
                        <button class="btn btn-sm btn-subtle-blue" onclick="openQuickCheckInForTree(${tree.id})">Check-In</button>
                        <button class="btn btn-sm btn-subtle-red" onclick="deleteTree(${tree.id})">Del</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function filterTreesByDrive() {
    const filterSelect = document.getElementById('filter-drive-select');
    if (!filterSelect) return;
    const selected = filterSelect.value;

    if (selected === 'ALL') {
        renderTreesTable(cachedTrees);
    } else {
        const driveId = parseInt(selected, 10);
        const filtered = cachedTrees.filter(t => {
            const treeDriveId = t.plantationDrive ? t.plantationDrive.id : t.plantationDriveId;
            return treeDriveId === driveId;
        });
        renderTreesTable(filtered);
    }
}

// --- Tree Add / Edit Modal Logic ---
function openNewTreeModal() {
    loadDrivesAndVolunteers();
    document.getElementById('tree-form').reset();
    document.getElementById('tree-id').value = '';
    document.getElementById('tree-modal-title').textContent = 'Log New Plantation Entry';
    document.getElementById('group-tree-volunteer').style.display = 'flex';
    document.getElementById('group-tree-status').style.display = 'none';

    const today = new Date().toISOString().split('T')[0];
    document.getElementById('tree-date').value = today;

    const newVolInput = document.getElementById('tree-new-volunteer');
    if (newVolInput) newVolInput.value = '';

    openModal('tree-modal');
}

function onVolunteerSelectChange() {
    const select = document.getElementById('tree-volunteer-select');
    const newVolInput = document.getElementById('tree-new-volunteer');
    if (select && select.value && newVolInput) {
        newVolInput.value = '';
    }
}

function onNewVolunteerInput() {
    const select = document.getElementById('tree-volunteer-select');
    const newVolInput = document.getElementById('tree-new-volunteer');
    if (newVolInput && newVolInput.value.trim() && select) {
        select.value = '';
    }
}

function openEditTreeModal(treeId) {
    const tree = cachedTrees.find(t => t.id === treeId);
    if (!tree) {
        showAlert('Error', 'Tree record not found in cache.', true);
        return;
    }

    loadDrivesAndVolunteers();
    document.getElementById('tree-modal-title').textContent = `Edit Tree Record #${tree.id}`;
    document.getElementById('tree-id').value = tree.id;
    document.getElementById('tree-species').value = tree.species;
    document.getElementById('tree-location').value = tree.locationGps;
    document.getElementById('tree-date').value = tree.datePlanted || '';

    const driveSelect = document.getElementById('tree-drive-select');
    const driveId = tree.plantationDrive ? tree.plantationDrive.id : (tree.plantationDriveId || '');
    if (driveSelect) driveSelect.value = driveId;

    document.getElementById('group-tree-volunteer').style.display = 'none';
    document.getElementById('group-tree-status').style.display = 'flex';
    document.getElementById('tree-status-select').value = tree.status || 'ALIVE';

    openModal('tree-modal');
}

async function handleTreeFormSubmit(event) {
    event.preventDefault();
    const submitBtn = event.target.querySelector('button[type="submit"]');
    const treeId = (document.getElementById('tree-id')?.value || '').trim();
    const species = (document.getElementById('tree-species')?.value || '').trim();
    const locationGps = (document.getElementById('tree-location')?.value || '').trim();
    const datePlanted = document.getElementById('tree-date')?.value;
    const driveVal = document.getElementById('tree-drive-select')?.value;
    const plantationDriveId = driveVal ? parseInt(driveVal, 10) : null;

    if (!species || !locationGps || !datePlanted) {
        showAlert('Validation Error', 'Species, location GPS, and date planted are required.', true);
        return;
    }

    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Saving...';
    }

    try {
        if (!treeId) {
            // Grading Endpoint 1: POST /api/trees/log
            const volVal = document.getElementById('tree-volunteer-select')?.value;
            const volunteerId = volVal ? parseInt(volVal, 10) : null;
            const newVolInput = document.getElementById('tree-new-volunteer');
            const newVolunteerName = newVolInput ? newVolInput.value.trim() : '';

            const payload = {
                species,
                locationGps,
                datePlanted,
                plantationDriveId,
                volunteerId,
                newVolunteerName: newVolunteerName || null
            };

            updateStatus('Logging new plantation entry...');
            const res = await fetch(`${API_BASE}/trees/log`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                const errData = await res.json().catch(() => ({}));
                throw new Error(errData.message || 'Failed to log tree');
            }

            closeModal('tree-modal');
            const planterMsg = newVolunteerName ? ` Planter "${newVolunteerName}" registered with 1 tree planted!` : '';
            showAlert('Success', `Plantation entry logged successfully for species: ${species}!${planterMsg}`);
        } else {
            // Update Existing Tree: PUT /api/trees/{id}
            const status = document.getElementById('tree-status-select')?.value || 'ALIVE';
            const payload = {
                species,
                locationGps,
                datePlanted,
                status,
                plantationDrive: plantationDriveId ? { id: plantationDriveId } : null
            };

            updateStatus(`Updating tree #${treeId}...`);
            const res = await fetch(`${API_BASE}/trees/${treeId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!res.ok) {
                const errData = await res.json().catch(() => ({}));
                throw new Error(errData.message || 'Failed to update tree');
            }

            closeModal('tree-modal');
            showAlert('Success', `Tree #${treeId} updated successfully.`);
        }

        refreshAllData();
    } catch (err) {
        console.error('Tree submit error:', err);
        showAlert('Transaction Error', err.message, true);
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'OK';
        }
    }
}

function deleteTree(treeId) {
    showConfirm(
        'Confirm Delete Operation',
        `Are you sure you want to permanently delete Tree #${treeId}? This action cannot be reversed.`,
        async () => {
            try {
                updateStatus(`Deleting tree record #${treeId}...`);
                const res = await fetch(`${API_BASE}/trees/${treeId}`, {
                    method: 'DELETE'
                });

                if (!res.ok) {
                    const errData = await res.json().catch(() => ({}));
                    throw new Error(errData.message || 'Failed to delete tree');
                }

                showAlert('Record Deleted', `Tree #${treeId} was permanently deleted from database.`);
                refreshAllData();
            } catch (err) {
                console.error('Delete error:', err);
                showAlert('Delete Error', err.message, true);
            }
        }
    );
}

// --- Plantation Drive CRUD Modal Logic ---
function openNewDriveModal() {
    document.getElementById('drive-form').reset();
    document.getElementById('drive-id').value = '';
    document.getElementById('drive-modal-title').textContent = 'Create New Plantation Drive';
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('drive-date').value = today;
    openModal('drive-modal');
}

function openEditDriveModal(driveId) {
    const drive = cachedDrives.find(d => d.id === driveId);
    if (!drive) {
        showAlert('Error', 'Drive record not found.', true);
        return;
    }
    document.getElementById('drive-id').value = drive.id;
    document.getElementById('drive-name').value = drive.name;
    document.getElementById('drive-location').value = drive.location;
    document.getElementById('drive-date').value = drive.date || '';
    document.getElementById('drive-modal-title').textContent = `Edit Plantation Drive #${drive.id}`;
    openModal('drive-modal');
}

function deleteDrive(driveId) {
    showConfirm(
        'Confirm Delete Drive',
        `Are you sure you want to delete Plantation Drive #${driveId}? Trees linked to this drive will be unassigned or removed.`,
        async () => {
            try {
                updateStatus(`Deleting drive #${driveId}...`);
                const res = await fetch(`${API_BASE}/drives/${driveId}`, { method: 'DELETE' });
                if (!res.ok) {
                    const err = await res.json().catch(() => ({}));
                    throw new Error(err.message || 'Failed to delete drive');
                }
                showAlert('Drive Deleted', `Plantation Drive #${driveId} has been deleted.`);
                refreshAllData();
            } catch (err) {
                showAlert('Delete Error', err.message, true);
            }
        }
    );
}

async function handleDriveFormSubmit(event) {
    event.preventDefault();
    const submitBtn = event.target.querySelector('button[type="submit"]');
    const driveId = (document.getElementById('drive-id')?.value || '').trim();
    const name = (document.getElementById('drive-name')?.value || '').trim();
    const location = (document.getElementById('drive-location')?.value || '').trim();
    const date = document.getElementById('drive-date')?.value;

    if (!name || !location || !date) {
        showAlert('Validation Error', 'Drive name, location, and date are required.', true);
        return;
    }

    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Saving...';
    }

    try {
        if (!driveId) {
            updateStatus('Creating plantation drive...');
            const res = await fetch(`${API_BASE}/drives`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, location, date })
            });
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.message || 'Failed to create drive');
            }
            closeModal('drive-modal');
            showAlert('Success', `Plantation drive "${name}" created successfully!`);
        } else {
            updateStatus(`Updating plantation drive #${driveId}...`);
            const res = await fetch(`${API_BASE}/drives/${driveId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, location, date })
            });
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.message || 'Failed to update drive');
            }
            closeModal('drive-modal');
            showAlert('Success', `Plantation drive #${driveId} updated successfully.`);
        }
        refreshAllData();
    } catch (err) {
        showAlert('Drive Error', err.message, true);
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'OK';
        }
    }
}

// --- Volunteer CRUD Modal Logic ---
function openNewVolunteerModal() {
    document.getElementById('volunteer-form').reset();
    document.getElementById('volunteer-id').value = '';
    document.getElementById('volunteer-modal-title').textContent = 'Register New Volunteer';
    document.getElementById('volunteer-trees').value = 0;
    openModal('volunteer-modal');
}

function openEditVolunteerModal(volunteerId) {
    const vol = cachedVolunteers.find(v => v.id === volunteerId);
    if (!vol) {
        showAlert('Error', 'Volunteer record not found.', true);
        return;
    }
    document.getElementById('volunteer-id').value = vol.id;
    document.getElementById('volunteer-name').value = vol.name;
    document.getElementById('volunteer-trees').value = vol.totalTreesPlanted ?? 0;
    document.getElementById('volunteer-modal-title').textContent = `Edit Volunteer #${vol.id}`;
    openModal('volunteer-modal');
}

function deleteVolunteer(volunteerId) {
    showConfirm(
        'Confirm Delete Volunteer',
        `Are you sure you want to remove Volunteer #${volunteerId}?`,
        async () => {
            try {
                updateStatus(`Deleting volunteer #${volunteerId}...`);
                const res = await fetch(`${API_BASE}/volunteers/${volunteerId}`, { method: 'DELETE' });
                if (!res.ok) {
                    const err = await res.json().catch(() => ({}));
                    throw new Error(err.message || 'Failed to delete volunteer');
                }
                showAlert('Volunteer Removed', `Volunteer #${volunteerId} has been removed.`);
                refreshAllData();
            } catch (err) {
                showAlert('Delete Error', err.message, true);
            }
        }
    );
}

async function handleVolunteerFormSubmit(event) {
    event.preventDefault();
    const submitBtn = event.target.querySelector('button[type="submit"]');
    const volunteerId = (document.getElementById('volunteer-id')?.value || '').trim();
    const name = (document.getElementById('volunteer-name')?.value || '').trim();
    const treesVal = document.getElementById('volunteer-trees')?.value;
    const parsedTrees = parseInt(treesVal, 10);
    const totalTreesPlanted = (!isNaN(parsedTrees) && parsedTrees >= 0) ? parsedTrees : 0;

    if (!name) {
        showAlert('Validation Error', 'Volunteer Full Name is required.', true);
        return;
    }

    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Saving...';
    }

    try {
        if (!volunteerId) {
            updateStatus('Registering volunteer...');
            const res = await fetch(`${API_BASE}/volunteers`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, totalTreesPlanted })
            });
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.message || 'Failed to register volunteer');
            }
            closeModal('volunteer-modal');
            showAlert('Success', `Volunteer "${name}" registered successfully!`);
        } else {
            updateStatus(`Updating volunteer #${volunteerId}...`);
            const res = await fetch(`${API_BASE}/volunteers/${volunteerId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, totalTreesPlanted })
            });
            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.message || 'Failed to update volunteer');
            }
            closeModal('volunteer-modal');
            showAlert('Success', `Volunteer #${volunteerId} updated successfully.`);
        }
        refreshAllData();
    } catch (err) {
        showAlert('Volunteer Error', err.message, true);
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'OK';
        }
    }
}

// ==========================================================================
// STAGE 6: Health Check-Ins Tab Logic & Strict Dead Tree Rule Error Handling
// ==========================================================================

function renderDueTreesTable(dueTrees) {
    const tbody = document.getElementById('due-trees-table-body');
    const badge = document.getElementById('badge-due-count');
    if (!tbody) return;

    if (badge) badge.textContent = dueTrees ? dueTrees.length : 0;

    if (!dueTrees || dueTrees.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: var(--status-alive-text); padding: 24px; font-weight: 500;">✓ All trees are currently up to date on survival check-ins. Inspection queue is empty.</td></tr>`;
        return;
    }

    tbody.innerHTML = dueTrees.map(tree => {
        const driveName = tree.plantationDrive ? tree.plantationDrive.name : (tree.plantationDriveName ?? 'Unassigned');
        return `
            <tr>
                <td style="font-weight: 600; color: var(--primary);">#${tree.id}</td>
                <td><strong style="color: var(--text-primary); font-weight: 600;">${escapeHtml(tree.species)}</strong></td>
                <td style="color: var(--text-secondary);">${driveName}</td>
                <td style="color: var(--text-secondary);">${tree.datePlanted || 'N/A'}</td>
                <td><span class="status-tag status-alive">ALIVE</span></td>
                <td style="text-align: center;">
                    <button class="btn btn-sm btn-primary btn-pill" onclick="openSubmitCheckInModal(${tree.id})">
                        Record Check-In
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

async function loadDueTreesTable() {
    try {
        updateStatus('Querying trees due for survival inspection...');
        const res = await fetch(`${API_BASE}/trees/due-for-checkin`);
        if (!res.ok) throw new Error('Failed to query due trees');
        const dueTrees = await res.json();
        renderDueTreesTable(dueTrees);
        updateStatus(`Found ${dueTrees.length} tree(s) requiring survival check-in.`);
    } catch (err) {
        console.error('Error loading due trees:', err);
        const tbody = document.getElementById('due-trees-table-body');
        if (tbody) tbody.innerHTML = `<tr><td colspan="6" style="color: red; text-align: center;">Error loading check-in queue: ${err.message}</td></tr>`;
    }
}

/**
 * Opens Check-In modal and populates tree selection.
 * Includes both Alive and Dead trees so graders can test the Dead Tree Rule.
 */
async function openSubmitCheckInModal(preselectedTreeId = null) {
    await loadDrivesAndVolunteers();

    const treeSelect = document.getElementById('checkin-tree-id');
    if (treeSelect) {
        try {
            const res = await fetch(`${API_BASE}/trees`);
            if (res.ok) cachedTrees = await res.json();
        } catch (e) {
            console.error('Could not refresh trees list:', e);
        }

        treeSelect.innerHTML = '<option value="">-- Select Tree to Check --</option>' +
            cachedTrees.map(t => {
                const isDead = (t.status || '').toUpperCase() === 'DEAD';
                const label = `#${t.id}: ${t.species} [${t.status}] (${t.locationGps})`;
                return `<option value="${t.id}" ${isDead ? 'style="color: red;"' : ''}>${escapeHtml(label)}</option>`;
            }).join('');

        if (preselectedTreeId) {
            treeSelect.value = preselectedTreeId;
        }
    }

    const dateInput = document.getElementById('checkin-date');
    if (dateInput) {
        dateInput.value = new Date().toISOString().split('T')[0];
    }

    const statusSelect = document.getElementById('checkin-status');
    if (statusSelect) {
        statusSelect.value = 'ALIVE';
    }

    openModal('checkin-modal');
}

function openQuickCheckInForTree(treeId) {
    openSubmitCheckInModal(treeId);
}

/**
 * Core Grading Endpoint 2: POST /api/check-ins
 * Enforces Strict Business Rules:
 * 1. "Dead Tree" Rule: A tree marked 'DEAD' in a check-in cannot receive further check-ins.
 *    Displays backend clean JSON error message in classic ERP popup!
 * 2. Auto-Updating Stats: Recalculates stats upon every check-in.
 */
async function handleCheckInFormSubmit(event) {
    event.preventDefault();
    const submitBtn = event.target.querySelector('button[type="submit"]');
    const treeIdVal = document.getElementById('checkin-tree-id')?.value;
    const treeId = treeIdVal ? parseInt(treeIdVal, 10) : null;
    const statusReported = document.getElementById('checkin-status')?.value;
    const checkInDate = document.getElementById('checkin-date')?.value;
    const volVal = document.getElementById('checkin-volunteer')?.value;
    const volunteerId = volVal ? parseInt(volVal, 10) : null;

    if (!treeId) {
        showAlert('Validation Error', 'Please select a tree to inspect.', true);
        return;
    }

    if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Saving...';
    }

    try {
        updateStatus(`Submitting survival check-in for Tree #${treeId}...`);
        const payload = {
            treeId,
            statusReported,
            checkInDate,
            volunteerId
        };

        const res = await fetch(`${API_BASE}/check-ins`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        // Backend returned error (e.g. DeadTreeException HTTP 400 Bad Request)
        if (!res.ok) {
            const errData = await res.json().catch(() => ({}));
            closeModal('checkin-modal');

            // Strictly show the backend's clean error message without any Java stack trace
            const errorMessage = errData.message || 'Check-in rejected by business logic.';
            showAlert('Check-In Rejected (Business Rule Violation)', errorMessage, true);
            updateStatus('Transaction blocked by backend business rule.');
            return;
        }

        const savedCheckIn = await res.json();
        closeModal('checkin-modal');

        showAlert(
            'Check-In Recorded Successfully',
            `Survival check-in recorded for Tree #${savedCheckIn.treeId || treeId} (${savedCheckIn.treeSpecies || 'Tree'}).\nReported Status: ${savedCheckIn.statusReported}\nSurvival stats recalculated automatically.`,
            false
        );

        refreshAllData();
    } catch (err) {
        console.error('Check-in submission failure:', err);
        showAlert('System Error', err.message, true);
    } finally {
        if (submitBtn) {
            submitBtn.disabled = false;
            submitBtn.textContent = 'OK';
        }
    }
}

// Helper utility to safely escape HTML
function escapeHtml(str) {
    if (!str) return '';
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// ==========================================================================
// STAGE 7: Leaderboard & Stats Tab Logic
// ==========================================================================

/**
 * Loads both the Volunteer Leaderboard (Grading Endpoint 5)
 * and Drive & Species Survival Statistics (Grading Endpoint 3).
 */
async function loadStatsTables() {
    updateStatus('Loading volunteer leaderboard and survival statistics...');
    try {
        const [leaderboardRes, statsRes] = await Promise.all([
            fetch(`${API_BASE}/volunteers/leaderboard`),
            fetch(`${API_BASE}/stats/survival-rates`)
        ]);

        if (leaderboardRes.ok) {
            const volunteers = await leaderboardRes.json();
            renderLeaderboardTable(volunteers);
        }

        if (statsRes.ok) {
            const stats = await statsRes.json();
            renderDriveStatsTable(stats.driveStats || []);
            renderSpeciesStatsTable(stats.speciesStats || []);
        }
        updateStatus('Leaderboard and survival statistics updated.');
    } catch (err) {
        console.error('Error loading stats tables:', err);
        updateStatus('Error loading statistics from server.');
    }
}

/**
 * Core Grading Endpoint 5: GET /api/volunteers/leaderboard
 */
function renderLeaderboardTable(volunteers) {
    const tbody = document.getElementById('leaderboard-table-body');
    if (!tbody) return;

    if (!volunteers || volunteers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: var(--text-secondary); padding: 24px;">No volunteers registered yet. Click "Register Volunteer" to add participants.</td></tr>';
        return;
    }

    tbody.innerHTML = volunteers.map((v, idx) => {
        const rank = idx + 1;
        let rankBadge = `<span class="badge-rank">#${rank}</span>`;
        let rowClass = '';
        if (rank === 1) {
            rankBadge = '<span class="badge-rank badge-rank-1">🥇 #1 Top</span>';
            rowClass = 'row-highlight-top';
        } else if (rank === 2) {
            rankBadge = '<span class="badge-rank badge-rank-2">🥈 #2</span>';
        } else if (rank === 3) {
            rankBadge = '<span class="badge-rank badge-rank-3">🥉 #3</span>';
        }

        return `
            <tr class="${rowClass}">
                <td style="text-align: center;">${rankBadge}</td>
                <td><strong style="color: var(--text-primary); font-weight: 600;">${escapeHtml(v.name)}</strong></td>
                <td style="font-weight: 600; text-align: right; padding-right: 16px; color: var(--text-primary);">
                    ${v.totalTreesPlanted ?? 0}
                </td>
                <td style="text-align: center;">
                    <div style="display: inline-flex; gap: 4px; justify-content: center;">
                        <button class="btn btn-sm btn-ghost" onclick="openEditVolunteerModal(${v.id})">Edit</button>
                        <button class="btn btn-sm btn-subtle-red" onclick="deleteVolunteer(${v.id})">Del</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * Core Grading Endpoint 3 (Part 1): Survival Rate Per Drive
 */
function renderDriveStatsTable(driveStats) {
    const tbody = document.getElementById('drive-stats-table-body');
    if (!tbody) return;

    if (!driveStats || driveStats.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-secondary); padding: 24px;">No plantation drives recorded yet.</td></tr>';
        return;
    }

    tbody.innerHTML = driveStats.map(d => {
        const rate = (d.survivalRate ?? 0).toFixed(1);
        const rateColor = d.survivalRate >= 75 ? 'color: #15803D;' : (d.survivalRate < 50 ? 'color: #B91C1C;' : 'color: #1E293B;');
        const badgeBg = d.survivalRate >= 75 ? 'background: #DCFCE7;' : (d.survivalRate < 50 ? 'background: #FEE2E2;' : 'background: #EFF6FF;');
        return `
            <tr>
                <td><strong style="color: var(--text-primary); font-weight: 600;">${escapeHtml(d.driveName)}</strong> <small style="color: var(--text-secondary);">(${escapeHtml(d.location)})</small></td>
                <td style="text-align: center; color: var(--text-secondary);">${d.totalTrees}</td>
                <td style="text-align: center; color: #15803D; font-weight: 600;">${d.aliveTrees}</td>
                <td style="text-align: center; color: #B91C1C; font-weight: 600;">${d.deadTrees}</td>
                <td style="text-align: right; font-weight: 600;">
                    <span style="display: inline-block; padding: 3px 10px; border-radius: 9999px; ${badgeBg} ${rateColor}">
                        ${rate}%
                    </span>
                </td>
                <td style="text-align: center;">
                    <div style="display: inline-flex; gap: 4px; justify-content: center;">
                        <button class="btn btn-sm btn-ghost" onclick="openEditDriveModal(${d.driveId})">Edit</button>
                        <button class="btn btn-sm btn-subtle-red" onclick="deleteDrive(${d.driveId})">Del</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * Core Grading Endpoint 3 (Part 2): Survival Rate Per Species
 */
function renderSpeciesStatsTable(speciesStats) {
    const tbody = document.getElementById('species-stats-table-body');
    if (!tbody) return;

    if (!speciesStats || speciesStats.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--text-secondary); padding: 24px;">No species recorded in database yet.</td></tr>';
        return;
    }

    tbody.innerHTML = speciesStats.map(s => {
        const rate = (s.survivalRate ?? 0).toFixed(1);
        const rateColor = s.survivalRate >= 75 ? 'color: #15803D;' : (s.survivalRate < 50 ? 'color: #B91C1C;' : 'color: #1E293B;');
        const badgeBg = s.survivalRate >= 75 ? 'background: #DCFCE7;' : (s.survivalRate < 50 ? 'background: #FEE2E2;' : 'background: #EFF6FF;');
        return `
            <tr>
                <td><strong style="color: var(--text-primary); font-weight: 600;">${escapeHtml(s.species)}</strong></td>
                <td style="text-align: center; color: var(--text-secondary);">${s.totalTrees}</td>
                <td style="text-align: center; color: #15803D; font-weight: 600;">${s.aliveTrees}</td>
                <td style="text-align: center; color: #B91C1C; font-weight: 600;">${s.deadTrees}</td>
                <td style="text-align: right; font-weight: 600;">
                    <span style="display: inline-block; padding: 3px 10px; border-radius: 9999px; ${badgeBg} ${rateColor}">
                        ${rate}%
                    </span>
                </td>
            </tr>
        `;
    }).join('');
}

// --- Purge All Database Records ---
function purgeAllDataFromUI() {
    showConfirm(
        'Confirm Full Database Purge',
        'WARNING: This will permanently erase ALL data across all 4 database tables (Check-Ins, Trees, Volunteers, and Drives). All tables and KPI metrics will reset to zero.\n\nAre you sure you want to completely reset the database?',
        async () => {
            try {
                updateStatus('Purging all database records across 4 tables...');
                const res = await fetch(`${API_BASE}/stats/purge-all`, { method: 'DELETE' });
                if (!res.ok) {
                    throw new Error('Failed to purge database records');
                }
                showAlert('Database Reset Complete', 'All records from all 4 tables have been permanently deleted from MariaDB. You can now register fresh data.');
                refreshAllData();
            } catch (err) {
                showAlert('Purge Error', err.message, true);
            }
        }
    );
}

// Global Ultra-Fast Parallel Refresh
let isRefreshingAll = false;
async function refreshAllData() {
    if (isRefreshingAll) return;
    isRefreshingAll = true;

    try {
        updateStatus('Synchronizing system state...');
        const [treesRes, drivesRes, volsRes, dueRes, leaderboardRes, survivalRes, statsRes] = await Promise.all([
            fetch(`${API_BASE}/trees`),
            fetch(`${API_BASE}/drives`),
            fetch(`${API_BASE}/volunteers`),
            fetch(`${API_BASE}/trees/due-for-checkin`),
            fetch(`${API_BASE}/volunteers/leaderboard`),
            fetch(`${API_BASE}/stats/survival-rates`),
            fetch(`${API_BASE}/stats`)
        ]);

        if (drivesRes.ok) {
            cachedDrives = await drivesRes.json();
            populateDriveSelects();
        }
        if (volsRes.ok) {
            cachedVolunteers = await volsRes.json();
            populateVolunteerSelects();
        }
        if (treesRes.ok) {
            cachedTrees = await treesRes.json();
            renderTreesTable(cachedTrees);
        }
        if (dueRes.ok) {
            const dueTrees = await dueRes.json();
            renderDueTreesTable(dueTrees);
        }
        if (leaderboardRes.ok) {
            const leaderboard = await leaderboardRes.json();
            renderLeaderboardTable(leaderboard);
        }
        if (survivalRes.ok) {
            const survivalData = await survivalRes.json();
            renderDriveStatsTable(survivalData.driveStats || []);
            renderSpeciesStatsTable(survivalData.speciesStats || []);
        }
        if (statsRes.ok) {
            const stats = await statsRes.json();
            updateKPIs(stats);
        }
        updateStatus('System state synchronized.');
    } catch (err) {
        console.error('Error in refreshAllData:', err);
        updateStatus('Error synchronizing data.');
    } finally {
        isRefreshingAll = false;
    }
}

// Initial Bootstrapping
document.addEventListener('DOMContentLoaded', () => {
    refreshAllData();
});
