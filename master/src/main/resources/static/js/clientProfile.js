// ================================
// Client Profile JavaScript
// ================================

// Global variables
let currentUser = null;
let userReservations = [];
let userReviews = [];
let currentFilter = 'all';

// Initialize page when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    loadUserProfile();
    loadReservations();
    loadReviews();
    loadFidelityData();
    initializeStarRating();
});

// Initialize page functionality
function initializePage() {
    // Profile form submission
    document.getElementById('profileForm').addEventListener('submit', function(e) {
        e.preventDefault();
        updateProfile();
    });

    // Review form setup
    setupReviewForm();
}

// Load user profile information
async function loadUserProfile() {
    try {
        // Fetch real user data from API
        const response = await fetch('/api/client/profile');
        if (!response.ok) {
            throw new Error('Failed to load profile');
        }
        
        currentUser = await response.json();
        displayUserProfile(currentUser);
    } catch (error) {
        console.error('Error loading profile:', error);
        
        // Fallback to mock data if API fails
        currentUser = {
            id: 1,
            firstName: "Jane",
            lastName: "Smith",
            email: "jane.smith@olh.fr",
            phone: "+33 4 90 04 96 77",
            address: "123 Rue de la Paix, 84000 Avignon, France"
        };
        
        displayUserProfile(currentUser);
        showErrorMessage('Utilisation de données de démonstration');
    }
}

// Display user profile information
function displayUserProfile(user) {
    document.getElementById('userFullName').textContent = `${user.firstName} ${user.lastName}`;
    document.getElementById('userEmail').textContent = user.email;
    
    // Fill form fields
    document.getElementById('firstName').value = user.firstName;
    document.getElementById('lastName').value = user.lastName;
    document.getElementById('email').value = user.email;
    document.getElementById('phone').value = user.phone || '';
    document.getElementById('address').value = user.address || '';
}

// Load user reservations (mock data for demo)
async function loadReservations() {
    try {
        // Mock reservations data - in real app, fetch from API
        userReservations = [
            {
                id: 1,
                roomNumber: "301",
                roomType: "Suite Présidentielle",
                roomImage: "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80",
                checkIn: "2024-12-15",
                checkOut: "2024-12-18",
                guests: 2,
                totalPrice: 1350,
                status: "past",
                canReview: true,
                hasReview: false
            },
            {
                id: 2,
                roomNumber: "201",
                roomType: "Chambre Deluxe",
                roomImage: "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80",
                checkIn: "2024-10-20",
                checkOut: "2024-10-23",
                guests: 2,
                totalPrice: 567,
                status: "past",
                canReview: true,
                hasReview: true
            },
            {
                id: 3,
                roomNumber: "401",
                roomType: "Chambre Familiale",
                roomImage: "https://images.unsplash.com/photo-1560472355-536de3962603?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80",
                checkIn: "2025-08-15",
                checkOut: "2025-08-20",
                guests: 4,
                totalPrice: 1145,
                status: "confirmed",
                canReview: false,
                hasReview: false
            },
            {
                id: 4,
                roomNumber: "102",
                roomType: "Chambre Standard",
                roomImage: "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?ixlib=rb-4.0.3&auto=format&fit=crop&w=300&q=80",
                checkIn: "2024-07-10",
                checkOut: "2024-07-12",
                guests: 2,
                totalPrice: 258,
                status: "cancelled",
                canReview: false,
                hasReview: false
            }
        ];

        displayReservations(userReservations);
    } catch (error) {
        console.error('Error loading reservations:', error);
        showErrorMessage('Erreur lors du chargement des réservations');
    }
}

// Display reservations
function displayReservations(reservations) {
    const container = document.getElementById('reservationsContainer');
    
    if (reservations.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-calendar-times"></i>
                <h5>Aucune réservation</h5>
                <p>Vous n'avez pas encore de réservations.</p>
                <a href="/clientHomePage" class="btn btn-primary">
                    <i class="fas fa-plus"></i> Faire une réservation
                </a>
            </div>
        `;
        return;
    }

    container.innerHTML = reservations.map(reservation => `
        <div class="reservation-card" data-status="${reservation.status}">
            <div class="row align-items-center">
                <div class="col-md-2">
                    <img src="${reservation.roomImage}" alt="${reservation.roomType}" class="room-image-small">
                </div>
                <div class="col-md-6">
                    <h6 class="mb-1">${reservation.roomNumber} - ${reservation.roomType}</h6>
                    <p class="text-muted mb-1">
                        <i class="fas fa-calendar"></i> 
                        ${formatDate(reservation.checkIn)} - ${formatDate(reservation.checkOut)}
                    </p>
                    <p class="text-muted mb-0">
                        <i class="fas fa-users"></i> ${reservation.guests} personne(s)
                    </p>
                </div>
                <div class="col-md-2 text-center">
                    <div class="reservation-status status-${reservation.status}">
                        ${getStatusText(reservation.status)}
                    </div>
                </div>
                <div class="col-md-2 text-end">
                    <div class="mb-2">
                        <strong>€${reservation.totalPrice}</strong>
                    </div>
                    <div class="review-actions">
                        ${reservation.canReview && !reservation.hasReview ? 
                            `<button class="btn btn-sm btn-warning" onclick="openReviewModal(${reservation.id})">
                                <i class="fas fa-star"></i> Donner un avis
                            </button>` : ''
                        }
                        ${reservation.hasReview ? 
                            `<span class="text-success"><i class="fas fa-check-circle"></i> Avis donné</span>` : ''
                        }
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

// Load user reviews (mock data)
async function loadReviews() {
    try {
        // Mock reviews data
        userReviews = [
            {
                id: 1,
                reservationId: 2,
                roomNumber: "201",
                roomType: "Chambre Deluxe",
                rating: 5,
                comment: "Séjour absolument fantastique ! La chambre était magnifique avec une vue imprenable sur les ocres. Le service était exceptionnel et le personnel très attentionné. Je recommande vivement !",
                cleanliness: 5,
                service: 5,
                comfort: 5,
                valueForMoney: 4,
                recommend: true,
                anonymous: false,
                date: "2024-10-25",
                checkIn: "2024-10-20",
                checkOut: "2024-10-23"
            }
        ];

        displayReviews(userReviews);
    } catch (error) {
        console.error('Error loading reviews:', error);
        showErrorMessage('Erreur lors du chargement des avis');
    }
}

// Display reviews
function displayReviews(reviews) {
    const container = document.getElementById('reviewsContainer');
    
    if (reviews.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-star"></i>
                <h5>Aucun avis</h5>
                <p>Vous n'avez pas encore laissé d'avis. Après un séjour, vous pourrez partager votre expérience.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = reviews.map(review => `
        <div class="review-card">
            <div class="d-flex justify-content-between align-items-start mb-3">
                <div>
                    <h6 class="mb-1">${review.roomNumber} - ${review.roomType}</h6>
                    <small class="text-muted">
                        Séjour du ${formatDate(review.checkIn)} au ${formatDate(review.checkOut)}
                    </small>
                </div>
                <div class="text-end">
                    <div class="rating-display mb-1">
                        ${generateStarDisplay(review.rating)}
                    </div>
                    <small class="text-muted">${formatDate(review.date)}</small>
                </div>
            </div>
            
            <div class="review-details mb-3">
                <div class="row">
                    <div class="col-md-3">
                        <small class="text-muted">Propreté:</small>
                        <div>${generateStarDisplay(review.cleanliness)}</div>
                    </div>
                    <div class="col-md-3">
                        <small class="text-muted">Service:</small>
                        <div>${generateStarDisplay(review.service)}</div>
                    </div>
                    <div class="col-md-3">
                        <small class="text-muted">Confort:</small>
                        <div>${generateStarDisplay(review.comfort)}</div>
                    </div>
                    <div class="col-md-3">
                        <small class="text-muted">Rapport Q/P:</small>
                        <div>${generateStarDisplay(review.valueForMoney)}</div>
                    </div>
                </div>
            </div>
            
            <p class="mb-3">${review.comment}</p>
            
            <div class="d-flex justify-content-between align-items-center">
                <div>
                    ${review.recommend ? 
                        '<span class="badge bg-success"><i class="fas fa-thumbs-up"></i> Recommande</span>' : 
                        '<span class="badge bg-secondary"><i class="fas fa-thumbs-down"></i> Ne recommande pas</span>'
                    }
                    ${review.anonymous ? 
                        '<span class="badge bg-info ms-2"><i class="fas fa-user-secret"></i> Anonyme</span>' : ''
                    }
                </div>
                <button class="btn btn-sm btn-outline-primary" onclick="editReview(${review.id})">
                    <i class="fas fa-edit"></i> Modifier
                </button>
            </div>
        </div>
    `).join('');
}

// Show specific section
function showSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('.content-section').forEach(section => {
        section.classList.remove('active');
    });
    
    // Remove active class from all menu items
    document.querySelectorAll('.menu-item').forEach(item => {
        item.classList.remove('active');
    });
    
    // Show selected section
    document.getElementById(sectionId).classList.add('active');
    
    // Add active class to clicked menu item
    document.querySelector(`[href="#${sectionId}"]`).classList.add('active');
}

// Filter reservations
function filterReservations(filter) {
    currentFilter = filter;
    
    // Update button states
    document.querySelectorAll('.filter-buttons .btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');
    
    // Filter reservations
    let filteredReservations = userReservations;
    if (filter !== 'all') {
        filteredReservations = userReservations.filter(r => r.status === filter);
    }
    
    displayReservations(filteredReservations);
}

// Open review modal
function openReviewModal(reservationId) {
    const reservation = userReservations.find(r => r.id === reservationId);
    if (!reservation) return;
    
    // Fill modal with reservation info
    document.getElementById('reservationId').value = reservationId;
    document.getElementById('reviewRoomInfo').textContent = `${reservation.roomNumber} - ${reservation.roomType}`;
    document.getElementById('reviewDateInfo').textContent = `${formatDate(reservation.checkIn)} - ${formatDate(reservation.checkOut)}`;
    
    // Reset form
    document.getElementById('reviewForm').reset();
    document.getElementById('rating').value = '';
    updateStarRating(0);
    
    // Show modal
    new bootstrap.Modal(document.getElementById('reviewModal')).show();
}

// Initialize star rating system
function initializeStarRating() {
    const stars = document.querySelectorAll('#starRating i');
    
    stars.forEach((star, index) => {
        star.addEventListener('click', () => {
            const rating = index + 1;
            document.getElementById('rating').value = rating;
            updateStarRating(rating);
        });
        
        star.addEventListener('mouseenter', () => {
            updateStarRating(index + 1, true);
        });
    });
    
    document.getElementById('starRating').addEventListener('mouseleave', () => {
        const currentRating = document.getElementById('rating').value || 0;
        updateStarRating(currentRating);
    });
}

// Update star rating display
function updateStarRating(rating, isHover = false) {
    const stars = document.querySelectorAll('#starRating i');
    
    stars.forEach((star, index) => {
        star.classList.remove('active');
        if (index < rating) {
            star.classList.add('active');
        }
    });
}

// Submit review
function submitReview() {
    const form = document.getElementById('reviewForm');
    const formData = new FormData(form);
    
    const reviewData = {
        reservationId: document.getElementById('reservationId').value,
        rating: document.getElementById('rating').value,
        cleanliness: document.getElementById('cleanliness').value,
        service: document.getElementById('service').value,
        comfort: document.getElementById('comfort').value,
        valueForMoney: document.getElementById('valueForMoney').value,
        comment: document.getElementById('reviewComment').value,
        recommend: document.getElementById('recommendHotel').checked,
        anonymous: document.getElementById('stayAnonymous').checked
    };
    
    // Validation
    if (!reviewData.rating) {
        showErrorMessage('Veuillez donner une note globale');
        return;
    }
    
    if (!reviewData.comment || reviewData.comment.length < 10) {
        showErrorMessage('Le commentaire doit contenir au moins 10 caractères');
        return;
    }
    
    // Simulate API call
    setTimeout(() => {
        // Update reservation status
        const reservation = userReservations.find(r => r.id == reviewData.reservationId);
        if (reservation) {
            reservation.hasReview = true;
        }
        
        // Add to reviews (for demo purposes)
        const newReview = {
            id: Date.now(),
            reservationId: reviewData.reservationId,
            roomNumber: reservation.roomNumber,
            roomType: reservation.roomType,
            rating: parseInt(reviewData.rating),
            comment: reviewData.comment,
            cleanliness: parseInt(reviewData.cleanliness) || 0,
            service: parseInt(reviewData.service) || 0,
            comfort: parseInt(reviewData.comfort) || 0,
            valueForMoney: parseInt(reviewData.valueForMoney) || 0,
            recommend: reviewData.recommend,
            anonymous: reviewData.anonymous,
            date: new Date().toISOString().split('T')[0],
            checkIn: reservation.checkIn,
            checkOut: reservation.checkOut
        };
        
        userReviews.unshift(newReview);
        
        // Refresh displays
        displayReservations(userReservations);
        displayReviews(userReviews);
        
        // Close modal and show success
        bootstrap.Modal.getInstance(document.getElementById('reviewModal')).hide();
        showSuccessMessage('Votre avis a été publié avec succès !');
    }, 1000);
}

// Update profile
function updateProfile() {
    const formData = {
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        phone: document.getElementById('phone').value,
        address: document.getElementById('address').value
    };
    
    // Simulate API call
    setTimeout(() => {
        // Update current user data
        Object.assign(currentUser, formData);
        
        // Update display
        document.getElementById('userFullName').textContent = `${formData.firstName} ${formData.lastName}`;
        
        showSuccessMessage('Profil mis à jour avec succès !');
    }, 1000);
}

// Reset profile form
function resetProfile() {
    displayUserProfile(currentUser);
}

// Edit review (for demo, just reopens modal with existing data)
function editReview(reviewId) {
    const review = userReviews.find(r => r.id === reviewId);
    if (!review) return;
    
    // Find corresponding reservation
    const reservation = userReservations.find(r => r.id === review.reservationId);
    if (!reservation) return;
    
    // Fill modal with existing review data
    document.getElementById('reservationId').value = review.reservationId;
    document.getElementById('reviewRoomInfo').textContent = `${review.roomNumber} - ${review.roomType}`;
    document.getElementById('reviewDateInfo').textContent = `${formatDate(review.checkIn)} - ${formatDate(review.checkOut)}`;
    
    // Fill form with existing data
    document.getElementById('rating').value = review.rating;
    document.getElementById('cleanliness').value = review.cleanliness || '';
    document.getElementById('service').value = review.service || '';
    document.getElementById('comfort').value = review.comfort || '';
    document.getElementById('valueForMoney').value = review.valueForMoney || '';
    document.getElementById('reviewComment').value = review.comment;
    document.getElementById('recommendHotel').checked = review.recommend;
    document.getElementById('stayAnonymous').checked = review.anonymous;
    
    updateStarRating(review.rating);
    
    // Show modal
    new bootstrap.Modal(document.getElementById('reviewModal')).show();
}

// Helper functions
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

function getStatusText(status) {
    const statusTexts = {
        'confirmed': 'Confirmée',
        'past': 'Terminée',
        'cancelled': 'Annulée',
        'pending': 'En attente'
    };
    return statusTexts[status] || status;
}

function generateStarDisplay(rating) {
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            stars += '<i class="fas fa-star"></i>';
        } else {
            stars += '<i class="fas fa-star empty"></i>';
        }
    }
    return stars;
}

function showSuccessMessage(message) {
    // Create toast or alert
    const toast = document.createElement('div');
    toast.className = 'alert alert-success position-fixed';
    toast.style.cssText = 'top: 100px; right: 20px; z-index: 9999;';
    toast.innerHTML = `
        <i class="fas fa-check-circle"></i> ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 5000);
}

function showErrorMessage(message) {
    // Create toast or alert
    const toast = document.createElement('div');
    toast.className = 'alert alert-danger position-fixed';
    toast.style.cssText = 'top: 100px; right: 20px; z-index: 9999;';
    toast.innerHTML = `
        <i class="fas fa-exclamation-circle"></i> ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 5000);
}

// Setup review form
function setupReviewForm() {
    // Add any additional form setup here
}

// ================================
// FIDELITY SYSTEM FUNCTIONS
// ================================

// Load fidelity data for the current user
async function loadFidelityData() {
    try {
        await Promise.all([
            loadFidelitySummary(),
            loadRedemptionOptions()
        ]);
    } catch (error) {
        console.error('Error loading fidelity data:', error);
        showErrorMessage('Erreur lors du chargement des données de fidélité');
    }
}

// Load fidelity summary
async function loadFidelitySummary() {
    try {
        const response = await fetch('/api/v1/fidelity/summary', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to load fidelity summary');
        }
        
        const fidelityData = await response.json();
        displayFidelitySummary(fidelityData);
        calculateFidelityStats();
    } catch (error) {
        console.error('Error loading fidelity summary:', error);
        showErrorMessage('Erreur lors du chargement des données de fidélité');
    }
}

// Display fidelity summary
function displayFidelitySummary(data) {
    const icon = document.getElementById('fidelityLevelIcon');
    const levelName = document.getElementById('fidelityLevelName');
    const currentPoints = document.getElementById('currentPoints');
    const pointsToNext = document.getElementById('pointsToNext');
    const discountBadge = document.getElementById('discountBadge');
    const progress = document.getElementById('fidelityProgress');
    
    // Set level icon and styling
    icon.className = `fas fa-medal fa-3x ${data.level.toLowerCase()}`;
    
    // Set level name with French translation
    const levelNames = {
        'BRONZE': 'Bronze',
        'SILVER': 'Argent', 
        'GOLD': 'Or',
        'DIAMOND': 'Diamant'
    };
    levelName.textContent = levelNames[data.level] || data.level;
    
    // Set points
    currentPoints.textContent = data.currentPoints;
    currentPoints.classList.add('points-animation');
    
    // Set progress bar
    const progressPercentage = calculateProgressPercentage(data.currentPoints, data.level);
    progress.style.width = progressPercentage + '%';
    progress.setAttribute('aria-valuenow', progressPercentage);
    
    // Set points to next level
    if (data.pointsToNextLevel > 0) {
        pointsToNext.textContent = `Points jusqu'au niveau suivant: ${data.pointsToNextLevel}`;
    } else {
        pointsToNext.textContent = 'Niveau maximum atteint !';
    }
    
    // Set discount badge
    const discountPercent = Math.round(data.discountPercentage * 100);
    discountBadge.textContent = `Réduction: ${discountPercent}%`;
    discountBadge.className = `badge fs-6 ${discountPercent > 0 ? 'bg-success' : 'bg-secondary'}`;
}

// Calculate progress percentage for the progress bar
function calculateProgressPercentage(currentPoints, level) {
    const levelRanges = {
        'BRONZE': { min: 0, max: 199 },
        'SILVER': { min: 200, max: 499 },
        'GOLD': { min: 500, max: 999 },
        'DIAMOND': { min: 1000, max: 1000 }
    };
    
    const range = levelRanges[level];
    if (!range) return 0;
    
    if (level === 'DIAMOND') return 100;
    
    const progressInLevel = currentPoints - range.min;
    const levelSize = range.max - range.min + 1;
    
    return Math.min(100, Math.max(0, (progressInLevel / levelSize) * 100));
}

// Calculate and display fidelity statistics
async function calculateFidelityStats() {
    try {
        // Get client reservations to calculate stats
        const reservationsResponse = await fetch('/api/v1/clients/me/reservations', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (reservationsResponse.ok) {
            const reservations = await reservationsResponse.json();
            
            // Calculate total nights from paid reservations
            const totalNights = reservations
                .filter(res => res.payed && new Date(res.reservationDateEnd) < new Date())
                .reduce((total, res) => {
                    const start = new Date(res.reservationDateStart);
                    const end = new Date(res.reservationDateEnd);
                    const nights = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
                    return total + nights;
                }, 0);
            
            // Calculate total earned points (approximation based on nights)
            const totalEarned = calculateTotalEarnedPoints(reservations);
            
            // For total redeemed, we'll use current points vs earned to estimate
            const currentPoints = parseInt(document.getElementById('currentPoints').textContent) || 0;
            const totalRedeemed = Math.max(0, totalEarned - currentPoints);
            
            document.getElementById('totalEarned').textContent = totalEarned;
            document.getElementById('totalRedeemed').textContent = totalRedeemed;
            document.getElementById('totalNights').textContent = totalNights;
        } else {
            // Fallback if reservations can't be loaded
            document.getElementById('totalEarned').textContent = '0';
            document.getElementById('totalRedeemed').textContent = '0';
            document.getElementById('totalNights').textContent = '0';
        }
    } catch (error) {
        console.error('Error calculating fidelity stats:', error);
        // Set default values on error
        document.getElementById('totalEarned').textContent = '0';
        document.getElementById('totalRedeemed').textContent = '0';
        document.getElementById('totalNights').textContent = '0';
    }
}

// Calculate total points that should have been earned from reservations
function calculateTotalEarnedPoints(reservations) {
    return reservations
        .filter(res => res.payed && new Date(res.reservationDateEnd) < new Date())
        .reduce((total, res) => {
            const start = new Date(res.reservationDateStart);
            const end = new Date(res.reservationDateEnd);
            const nights = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
            
            let points = nights * 10; // Base: 10 points per night
            
            // Long stay bonus
            if (nights > 7) {
                points += 50;
            }
            
            // Early booking bonus (if reservation was made 30+ days in advance)
            const reservationMade = new Date(res.createdAt || res.reservationDateStart);
            const daysBefore = Math.ceil((start - reservationMade) / (1000 * 60 * 60 * 24));
            if (daysBefore >= 30) {
                points += 25;
            }
            
            return total + points;
        }, 0);
}

// Load redemption options
async function loadRedemptionOptions() {
    try {
        const response = await fetch('/api/v1/fidelity/redemption-options', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Failed to load redemption options');
        }
        
        const options = await response.json();
        displayRedemptionOptions(options);
    } catch (error) {
        console.error('Error loading redemption options:', error);
        // Show error state
        const container = document.getElementById('redemptionOptions');
        container.innerHTML = `
            <div class="col-12">
                <div class="empty-state">
                    <i class="fas fa-exclamation-triangle text-warning"></i>
                    <h5>Erreur de chargement</h5>
                    <p>Impossible de charger les options d'échange.</p>
                    <button class="btn btn-outline-primary" onclick="loadRedemptionOptions()">
                        <i class="fas fa-refresh"></i> Réessayer
                    </button>
                </div>
            </div>
        `;
    }
}

// Display redemption options
function displayRedemptionOptions(options) {
    const container = document.getElementById('redemptionOptions');
    
    if (options.length === 0) {
        container.innerHTML = `
            <div class="col-12">
                <div class="empty-state">
                    <i class="fas fa-gift"></i>
                    <h5>Aucune option d'échange disponible</h5>
                    <p>Gagnez plus de points en effectuant des réservations.</p>
                </div>
            </div>
        `;
        return;
    }
    
    container.innerHTML = options.map(option => `
        <div class="col-md-6 col-lg-4 redemption-option">
            <div class="redemption-card ${option.available ? 'available' : 'unavailable'}" 
                 onclick="${option.available ? `redeemOption('${option.id}', ${option.pointsCost})` : ''}">
                <div class="position-relative">
                    <span class="redemption-type-badge badge bg-primary">${getTypeIcon(option.type)}</span>
                    <div class="points-cost">${option.pointsCost} points</div>
                    <h6 class="fw-bold">${option.title}</h6>
                    <p class="text-muted small">${option.description}</p>
                    ${!option.available ? '<p class="text-danger small"><i class="fas fa-lock"></i> Points insuffisants</p>' : ''}
                </div>
            </div>
        </div>
    `).join('');
}

// Get icon for redemption type
function getTypeIcon(type) {
    const icons = {
        'discount': '<i class="fas fa-percent"></i>',
        'upgrade': '<i class="fas fa-arrow-up"></i>',
        'service': '<i class="fas fa-concierge-bell"></i>',
        'amenity': '<i class="fas fa-glass-cheers"></i>'
    };
    return icons[type] || '<i class="fas fa-gift"></i>';
}

// Redeem a fidelity option
async function redeemOption(optionId, pointsCost) {
    const confirmed = confirm(`Êtes-vous sûr de vouloir échanger ${pointsCost} points contre cette récompense ?`);
    
    if (!confirmed) return;
    
    try {
        const response = await fetch('/api/v1/fidelity/redeem', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                optionId: optionId,
                points: pointsCost
            })
        });
        
        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Erreur lors de l\'échange des points');
        }
        
        const result = await response.json();
        
        if (result.success) {
            // Update points display
            document.getElementById('currentPoints').textContent = result.remainingPoints;
            document.getElementById('currentPoints').classList.add('points-animation');
            
            showSuccessMessage(result.message || `Échange réussi ! Il vous reste ${result.remainingPoints} points.`);
            
            // Reload data to update everything
            setTimeout(() => {
                loadRedemptionOptions();
                loadFidelitySummary();
            }, 1000);
        } else {
            showErrorMessage(result.message || 'Erreur lors de l\'échange des points');
        }
        
    } catch (error) {
        console.error('Error redeeming points:', error);
        showErrorMessage(error.message || 'Erreur lors de l\'échange des points. Veuillez réessayer.');
    }
}

// Recalculate points based on reservation history
async function recalculatePoints() {
    try {
        const button = event.target;
        const originalContent = button.innerHTML;
        
        // Show loading state
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Recalcul...';
        button.disabled = true;
        
        const response = await fetch('/api/v1/fidelity/recalculate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Erreur lors du recalcul des points');
        }
        
        const result = await response.json();
        
        if (result.success) {
            // Update points display
            document.getElementById('currentPoints').textContent = result.newTotal;
            document.getElementById('currentPoints').classList.add('points-animation');
            
            showSuccessMessage(result.message || `Points recalculés ! Nouveau total: ${result.newTotal} points.`);
            
            // Reload fidelity data
            setTimeout(() => {
                loadFidelitySummary();
                calculateFidelityStats();
            }, 1000);
        } else {
            showErrorMessage(result.message || 'Erreur lors du recalcul des points');
        }
        
    } catch (error) {
        console.error('Error recalculating points:', error);
        showErrorMessage('Erreur lors du recalcul des points.');
    } finally {
        // Restore button
        const button = event.target;
        button.innerHTML = '<i class="fas fa-sync-alt"></i> Recalculer les points';
        button.disabled = false;
    }
}

