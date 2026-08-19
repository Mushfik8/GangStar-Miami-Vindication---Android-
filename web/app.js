// GangStar Miami Vindication - Touch & Controls Controller
document.addEventListener('DOMContentLoaded', () => {
    const btnModeToggle = document.getElementById('btnModeToggle');
    const btnOpacityToggle = document.getElementById('btnOpacityToggle');
    const layerDriving = document.getElementById('layerDriving');
    const layerFoot = document.getElementById('layerFoot');
    const touchHud = document.getElementById('touchHud');
    const btnStartGame = document.getElementById('btnStartGame');
    const loadingOverlay = document.getElementById('loadingOverlay');
    const gameContainer = document.getElementById('gameContainer');

    let currentMode = 'DRIVING'; // 'DRIVING' | 'FOOT'
    let currentOpacity = 0.70;
    const activeKeys = new Set();

    // Mode Toggle
    btnModeToggle.addEventListener('click', () => {
        if (currentMode === 'DRIVING') {
            currentMode = 'FOOT';
            btnModeToggle.textContent = '🚶 ON-FOOT MODE';
            layerDriving.classList.remove('active');
            layerFoot.classList.add('active');
        } else {
            currentMode = 'DRIVING';
            btnModeToggle.textContent = '🚗 DRIVING MODE';
            layerFoot.classList.remove('active');
            layerDriving.classList.add('active');
        }
        releaseAllKeys();
    });

    // Opacity Toggle
    btnOpacityToggle.addEventListener('click', () => {
        if (currentOpacity >= 0.90) {
            currentOpacity = 0.30;
        } else if (currentOpacity >= 0.70) {
            currentOpacity = 0.90;
        } else if (currentOpacity >= 0.50) {
            currentOpacity = 0.70;
        } else {
            currentOpacity = 0.50;
        }
        touchHud.style.opacity = currentOpacity;
        btnOpacityToggle.textContent = `👁 HUD: ${Math.round(currentOpacity * 100)}%`;
    });

    // --- Touch & Hold-To-Move Button Logic ---
    const allButtons = document.querySelectorAll('.hud-btn');

    function pressKey(keyCode, buttonEl) {
        if (!keyCode) return;
        activeKeys.add(keyCode);
        if (buttonEl) buttonEl.classList.add('active');
        dispatchJ2MEKeyEvent('keydown', keyCode);
    }

    function releaseKey(keyCode, buttonEl) {
        if (!keyCode) return;
        activeKeys.delete(keyCode);
        if (buttonEl) buttonEl.classList.remove('active');
        dispatchJ2MEKeyEvent('keyup', keyCode);
    }

    function releaseAllKeys() {
        activeKeys.forEach(code => {
            dispatchJ2MEKeyEvent('keyup', code);
        });
        activeKeys.clear();
        allButtons.forEach(b => b.classList.remove('active'));
    }

    allButtons.forEach(btn => {
        const keyCode = parseInt(btn.dataset.key, 10);

        // Touch handling (Multi-touch support)
        btn.addEventListener('touchstart', (e) => {
            e.preventDefault();
            pressKey(keyCode, btn);
        }, { passive: false });

        btn.addEventListener('touchend', (e) => {
            e.preventDefault();
            releaseKey(keyCode, btn);
        }, { passive: false });

        btn.addEventListener('touchcancel', (e) => {
            e.preventDefault();
            releaseKey(keyCode, btn);
        }, { passive: false });

        // Mouse handling
        btn.addEventListener('mousedown', (e) => {
            e.preventDefault();
            pressKey(keyCode, btn);
        });

        btn.addEventListener('mouseup', (e) => {
            e.preventDefault();
            releaseKey(keyCode, btn);
        });

        btn.addEventListener('mouseleave', (e) => {
            if (activeKeys.has(keyCode)) {
                releaseKey(keyCode, btn);
            }
        });
    });

    // --- Physical Keyboard Controls ---
    const KEY_MAPPINGS = {
        'KeyW': 50,      // 2 / Up / Gas
        'ArrowUp': 50,
        'KeyS': 56,      // 8 / Down / Brake
        'ArrowDown': 56,
        'KeyA': 52,      // 4 / Left / Steer Left
        'ArrowLeft': 52,
        'KeyD': 54,      // 6 / Right / Steer Right
        'ArrowRight': 54,
        'Space': 53,     // 5 / Fire / Action
        'KeyE': 53,      // Enter car / Action
        'ShiftLeft': 55, // 7 / Handbrake / Drift
        'ShiftRight': 55,
        'KeyQ': 49,      // 1 / Prev Weapon
        'KeyR': 51,      // 3 / Next Weapon
        'KeyH': 35,      // # / Horn
        'Escape': -6,    // Softkey 1 / Pause
        'Enter': -7      // Softkey 2 / Menu
    };

    window.addEventListener('keydown', (e) => {
        if (KEY_MAPPINGS[e.code]) {
            const jKey = KEY_MAPPINGS[e.code];
            const btnEl = document.querySelector(`.hud-btn[data-key="${jKey}"]`);
            pressKey(jKey, btnEl);
        }
    });

    window.addEventListener('keyup', (e) => {
        if (KEY_MAPPINGS[e.code]) {
            const jKey = KEY_MAPPINGS[e.code];
            const btnEl = document.querySelector(`.hud-btn[data-key="${jKey}"]`);
            releaseKey(jKey, btnEl);
        }
    });

    function dispatchJ2MEKeyEvent(type, keyCode) {
        // Dispatches to canvas / active game instance
        const event = new CustomEvent('j2me-key', { detail: { type, keyCode } });
        window.dispatchEvent(event);

        // Also trigger standard keyboard events on document/canvas
        let browserKey = '5';
        if (keyCode === 50) browserKey = '2';
        if (keyCode === 56) browserKey = '8';
        if (keyCode === 52) browserKey = '4';
        if (keyCode === 54) browserKey = '6';
        if (keyCode === 53) browserKey = '5';
        if (keyCode === 55) browserKey = '7';
        if (keyCode === 49) browserKey = '1';
        if (keyCode === 51) browserKey = '3';

        const kbEvent = new KeyboardEvent(type, {
            key: browserKey,
            which: keyCode > 0 ? keyCode : 13,
            bubbles: true
        });
        document.dispatchEvent(kbEvent);
    }

    // Start Game Button
    btnStartGame.addEventListener('click', async () => {
        loadingOverlay.querySelector('.loading-text').textContent = 'INITIALIZING J2ME RUNTIME...';
        btnStartGame.style.display = 'none';

        try {
            if (typeof cheerpjInit === 'function') {
                await cheerpjInit();
                await cheerpjCreateDisplay(800, 480, gameContainer);
                await cheerpjRunJar('/app/Gangstar_Miami.jar');
            } else {
                startCanvasFallback();
            }
        } catch (err) {
            console.warn('CheerpJ online loader fallback:', err);
            startCanvasFallback();
        }
    });

    function startCanvasFallback() {
        loadingOverlay.style.display = 'none';
        const canvas = document.createElement('canvas');
        canvas.width = 800;
        canvas.height = 480;
        canvas.id = 'gameCanvas';
        gameContainer.appendChild(canvas);

        const ctx = canvas.getContext('2d');
        let frame = 0;

        function render() {
            frame++;
            ctx.fillStyle = '#0f172a';
            ctx.fillRect(0, 0, 800, 480);

            // Draw grid & skyline
            ctx.strokeStyle = 'rgba(0, 240, 255, 0.15)';
            ctx.lineWidth = 1;
            for (let x = 0; x < 800; x += 40) {
                ctx.beginPath();
                ctx.moveTo(x, 0);
                ctx.lineTo(x, 480);
                ctx.stroke();
            }
            for (let y = 0; y < 480; y += 40) {
                ctx.beginPath();
                ctx.moveTo(0, y);
                ctx.lineTo(800, y);
                ctx.stroke();
            }

            // Title & Status
            ctx.fillStyle = '#ff007f';
            ctx.font = 'bold 28px Outfit, sans-serif';
            ctx.textAlign = 'center';
            ctx.fillText('GANGSTAR: MIAMI VINDICATION', 400, 160);

            ctx.fillStyle = '#00f0ff';
            ctx.font = '16px Outfit, sans-serif';
            ctx.fillText('GTA VICE CITY MOBILE TOUCH ENGINE ACTIVE', 400, 200);

            // Active Inputs display
            ctx.fillStyle = '#00ff88';
            ctx.font = '14px monospace';
            const keysArray = Array.from(activeKeys);
            ctx.fillText(`ACTIVE KEYS HELD: [ ${keysArray.join(', ') || 'NONE - (HOLD BUTTONS TO DRIVE)'} ]`, 400, 270);

            // Car / Player Simulation Preview
            ctx.fillStyle = activeKeys.has(50) ? '#00ff88' : (activeKeys.has(56) ? '#ff3344' : '#ffaa00');
            ctx.fillRect(360, 320, 80, 40);
            ctx.fillStyle = '#fff';
            ctx.font = 'bold 12px sans-serif';
            ctx.fillText(activeKeys.has(50) ? 'DRIVING ▲' : (activeKeys.has(56) ? 'REVERSING ▼' : 'IDLE / STOPPED'), 400, 345);

            requestAnimationFrame(render);
        }
        render();
    }
});
