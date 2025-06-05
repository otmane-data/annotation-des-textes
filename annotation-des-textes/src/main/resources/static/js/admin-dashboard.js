document.addEventListener('DOMContentLoaded', function() {
    // Configuration des graphiques
    Chart.defaults.font.family = "'Inter', 'Helvetica', 'Arial', sans-serif";
    Chart.defaults.color = '#6c757d';

    // Graphique des annotations
    const annotationsCtx = document.getElementById('annotationsChart').getContext('2d');
    const annotationsChart = new Chart(annotationsCtx, {
        type: 'line',
        data: {
            labels: ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'],
            datasets: [{
                label: 'Annotations',
                data: annotationsData, // données injectées par Thymeleaf
                borderColor: '#1a237e',
                backgroundColor: 'rgba(26, 35, 126, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        drawBorder: false
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            }
        }
    });

    // Graphique de distribution
    const distributionCtx = document.getElementById('distributionChart').getContext('2d');
    const distributionChart = new Chart(distributionCtx, {
        type: 'doughnut',
        data: {
            labels: ['Entailment', 'Contradiction', 'Neutral'],
            datasets: [{
                data: distributionData, // données injectées par Thymeleaf
                backgroundColor: [
                    '#1a237e',
                    '#283593',
                    '#3949ab'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });

    // Gestion des filtres de période
    const periodButtons = document.querySelectorAll('.btn-group .btn');
    periodButtons.forEach(button => {
        button.addEventListener('click', function() {
            periodButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            
            // Ici, vous pouvez ajouter la logique pour mettre à jour les données
            // en fonction de la période sélectionnée
        });
    });

    // Initialiser les tooltips Bootstrap
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Rafraîchissement automatique des données
    setInterval(function() {
        // Ici, vous pouvez ajouter la logique pour rafraîchir les données
        // via une requête AJAX
    }, 300000); // Rafraîchir toutes les 5 minutes
});
