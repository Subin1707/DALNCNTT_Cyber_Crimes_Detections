(function () {
    const state = {
        algorithms: [],
        bestAlgorithm: null
    };

    const refs = {
        kInput: document.getElementById("kInput"),
        repeatInput: document.getElementById("repeatInput"),
        reloadBtn: document.getElementById("reloadComparisonBtn"),
        tableStatus: document.getElementById("tableStatus"),
        body: document.getElementById("comparisonBody"),
        matrixGrid: document.getElementById("matrixGrid"),
        bestName: document.getElementById("bestName"),
        bestReason: document.getElementById("bestReason"),
        bestScore: document.getElementById("bestScore"),
        trainCount: document.getElementById("trainCount"),
        testCount: document.getElementById("testCount"),
        datasetSource: document.getElementById("datasetSource"),
        datasetPath: document.getElementById("datasetPath"),
        labelDistribution: document.getElementById("labelDistribution"),
        bestAccuracy: document.getElementById("bestAccuracy"),
        bestTime: document.getElementById("bestTime"),
        testRunInfo: document.getElementById("testRunInfo"),
        rankingFormula: document.getElementById("rankingFormula"),
        evaluationProtocol: document.getElementById("evaluationProtocol"),
        groundTruthField: document.getElementById("groundTruthField"),
        classifierInputFields: document.getElementById("classifierInputFields")
    };

    function percent(value) {
        return `${(Number(value || 0) * 100).toFixed(2)}%`;
    }

    function score(value) {
        return Number(value || 0).toFixed(3);
    }

    function shortFormula(value) {
        if (!value) return "--";
        return value
            .replace("optimizationScore = ", "")
            .replace("accuracy", "Acc")
            .replace("speed", "Speed");
    }

    function ms(value) {
        const number = Number(value || 0);
        if (number < 0.001) return "< 0.001 ms";
        return `${number.toFixed(4)} ms`;
    }

    function regionValue(matrix, actual, predicted) {
        return Number(matrix?.[actual]?.[predicted] || 0);
    }

    function setLoading(isLoading) {
        refs.tableStatus.textContent = isLoading ? "Đang tải" : "Đã cập nhật";
        refs.reloadBtn.disabled = isLoading;
    }

    function renderBest(report) {
        const best = report.bestAlgorithm;
        refs.bestName.textContent = best ? best.name : "Chưa có dữ liệu";
        refs.bestReason.textContent = best
            ? `${best.name} đang tối ưu nhất theo trung bình ${best.runCount} lần test: Accuracy ${percent(best.accuracy)}, F1 ${percent(best.f1Score)}, dao động ${percent(best.accuracyMin)}-${percent(best.accuracyMax)}.`
            : "Không có kết quả đánh giá.";
        refs.bestScore.textContent = best ? score(best.optimizationScore) : "--";
        refs.trainCount.textContent = String(report.trainCount || "--");
        refs.testCount.textContent = String(report.testCount || "--");
        refs.datasetSource.textContent = report.datasetSource || "--";
        refs.datasetPath.textContent = report.datasetPath || "--";
        refs.labelDistribution.textContent = formatLabelDistribution(report.labelDistribution);
        refs.bestAccuracy.textContent = best ? percent(best.accuracy) : "--";
        refs.bestTime.textContent = best ? ms(best.averageProcessingMs) : "--";
        refs.testRunInfo.textContent = `${report.testSampleSizePerRun || "--"} x ${report.repeatCount || "--"} lần`;
        refs.rankingFormula.textContent = shortFormula(report.rankingFormula);
        refs.evaluationProtocol.textContent = report.evaluationProtocol || "--";
        refs.groundTruthField.textContent = report.groundTruthField || "--";
        refs.classifierInputFields.textContent = report.classifierInputFields || "--";
    }

    function formatLabelDistribution(distribution) {
        if (!distribution) return "--";
        return Object.entries(distribution)
            .map(([label, count]) => `${label}: ${count}`)
            .join(" | ");
    }

    function renderTable(algorithms) {
        refs.body.innerHTML = "";

        algorithms.forEach((algorithm, index) => {
            const row = document.createElement("tr");
            if (index === 0) row.classList.add("rank-one");

            row.innerHTML = `
                <td>
                    <div class="algorithm-name">${algorithm.name}</div>
                    <small>${algorithm.methodType}</small>
                </td>
                <td><span class="family-pill">${algorithm.family}</span></td>
                <td>${algorithm.correctPredictions}/${algorithm.totalPredictions}</td>
                <td class="percent-cell">${percentBlock(algorithm.accuracy)}</td>
                <td class="percent-cell">${percentBlock(algorithm.precision)}</td>
                <td class="percent-cell">${percentBlock(algorithm.recall)}</td>
                <td class="percent-cell">${percentBlock(algorithm.f1Score)}</td>
                <td>${percent(algorithm.accuracyMin)} - ${percent(algorithm.accuracyMax)}</td>
                <td>${percent(algorithm.accuracyStdDev)}</td>
                <td>${ms(algorithm.averageProcessingMs)}</td>
                <td>${score(algorithm.optimizationScore)}</td>
                <td>${algorithm.note}</td>
            `;
            refs.body.appendChild(row);
        });
    }

    function percentBlock(value) {
        const width = Math.max(0, Math.min(100, Number(value || 0) * 100));
        return `
            <strong>${percent(value)}</strong>
            <div class="bar" aria-hidden="true"><span style="width:${width}%"></span></div>
        `;
    }

    function renderMatrices(algorithms) {
        refs.matrixGrid.innerHTML = "";
        const regions = ["SAFE", "SUSPICIOUS", "FRAUD"];
        const runIndexes = Array.from(new Set(
            algorithms.flatMap((algorithm) => (algorithm.runMetrics || []).map((run) => run.runIndex))
        )).sort((a, b) => a - b);

        if (!runIndexes.length) {
            algorithms.forEach((algorithm) => {
                refs.matrixGrid.appendChild(createMatrixPanel(algorithm.name, algorithm.confusionMatrix, regions));
            });
            return;
        }

        runIndexes.forEach((runIndex) => {
            const runBlock = document.createElement("section");
            runBlock.className = "run-comparison";
            runBlock.innerHTML = `
                <div class="run-head">
                    <h3>Lần so sánh ${runIndex}</h3>
                    <span>${algorithms.length} thuật toán</span>
                </div>
                <div class="run-matrix-grid"></div>
            `;

            const runGrid = runBlock.querySelector(".run-matrix-grid");
            algorithms.forEach((algorithm) => {
                const run = (algorithm.runMetrics || []).find((item) => item.runIndex === runIndex);
                const subtitle = run
                    ? `${run.correctPredictions}/${run.totalPredictions} đúng - Accuracy ${percent(run.accuracy)}`
                    : "Không có dữ liệu lần này";
                runGrid.appendChild(createMatrixPanel(algorithm.name, run?.confusionMatrix, regions, subtitle));
            });
            refs.matrixGrid.appendChild(runBlock);
        });
    }

    function createMatrixPanel(title, matrix, regions, subtitle = "") {
            const panel = document.createElement("article");
            panel.className = "matrix-panel";
            panel.innerHTML = `
                <h4>${title}</h4>
                ${subtitle ? `<p>${subtitle}</p>` : ""}
                <table class="mini-matrix">
                    <thead>
                    <tr>
                        <th>Actual \\ Pred</th>
                        ${regions.map((region) => `<th>${region}</th>`).join("")}
                    </tr>
                    </thead>
                    <tbody>
                    ${regions.map((actual) => `
                        <tr>
                            <th>${actual}</th>
                            ${regions.map((predicted) => {
                                const value = regionValue(matrix, actual, predicted);
                                const className = actual === predicted ? "hit-cell" : "";
                                return `<td class="${className}">${value}</td>`;
                            }).join("")}
                        </tr>
                    `).join("")}
                    </tbody>
                </table>
            `;
            return panel;
    }

    async function loadComparison() {
        setLoading(true);
        let succeeded = false;
        const k = encodeURIComponent(refs.kInput.value || "5");
        const repeatCount = encodeURIComponent(refs.repeatInput.value || "30");

        try {
            const response = await fetch(`/api/algorithm-comparison?k=${k}&repeatCount=${repeatCount}`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            const report = await response.json();
            state.algorithms = report.algorithms || [];
            state.bestAlgorithm = report.bestAlgorithm || null;
            renderBest(report);
            renderTable(state.algorithms);
            renderMatrices(state.algorithms);
            succeeded = true;
        } catch (error) {
            refs.tableStatus.textContent = "Lỗi tải dữ liệu";
            refs.bestName.textContent = "Không tải được dữ liệu";
            refs.bestReason.textContent = error.message;
            refs.body.innerHTML = "";
            refs.matrixGrid.innerHTML = "";
        } finally {
            refs.reloadBtn.disabled = false;
            if (succeeded) {
                refs.tableStatus.textContent = "Đã cập nhật";
            }
        }
    }

    refs.reloadBtn.addEventListener("click", loadComparison);
    loadComparison();
})();
