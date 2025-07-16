// ================================
// Client Home Page JavaScript
// ================================

// Global variables
let currentRooms = [];
let currentReviews = [];
let reviewsOffset = 0;
const reviewsLimit = 6;

// Initialize page when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializePage();
    loadRooms();
    loadReviews();
    setMinDate();
});

// Initialize page functionality
function initializePage() {
    // Smooth scrolling for navigation links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });

    // Set default dates
    const today = new Date();
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    const nextDay = new Date(tomorrow);
    nextDay.setDate(nextDay.getDate() + 1);

    document.getElementById('checkInDate').value = tomorrow.toISOString().split('T')[0];
    document.getElementById('checkOutDate').value = nextDay.toISOString().split('T')[0];
}

// Set minimum date for date inputs
function setMinDate() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('checkInDate').min = today;
    document.getElementById('checkOutDate').min = today;

    // Update checkout minimum when checkin changes
    document.getElementById('checkInDate').addEventListener('change', function() {
        const checkinDate = new Date(this.value);
        const checkoutDate = new Date(checkinDate);
        checkoutDate.setDate(checkoutDate.getDate() + 1);
        
        document.getElementById('checkOutDate').min = checkoutDate.toISOString().split('T')[0];
        
        if (new Date(document.getElementById('checkOutDate').value) <= checkinDate) {
            document.getElementById('checkOutDate').value = checkoutDate.toISOString().split('T')[0];
        }
    });
}

// Load and display rooms
function loadRooms() {
    showLoading('roomsContainer');
    
    // Simulate API call - replace with actual API endpoint
    setTimeout(() => {
        const rooms = getSampleRooms();
        displayRooms(rooms);
        currentRooms = rooms;
    }, 1000);
}

// Search rooms based on criteria
function searchRooms() {
    const checkIn = document.getElementById('checkInDate').value;
    const checkOut = document.getElementById('checkOutDate').value;
    const adults = document.getElementById('adults').value;
    const children = document.getElementById('children').value;

    if (!checkIn || !checkOut) {
        alert('Veuillez sélectionner les dates d\'arrivée et de départ');
        return;
    }

    if (new Date(checkIn) >= new Date(checkOut)) {
        alert('La date de départ doit être après la date d\'arrivée');
        return;
    }

    showLoading('roomsContainer');

    // Simulate API search - replace with actual API call
    setTimeout(() => {
        const filteredRooms = currentRooms.filter(room => {
            return room.maxOccupancy >= (parseInt(adults) + parseInt(children));
        });
        displayRooms(filteredRooms);
    }, 800);
}

// Display rooms in the container
function displayRooms(rooms) {
    const container = document.getElementById('roomsContainer');
    
    if (rooms.length === 0) {
        container.innerHTML = `
            <div class="col-12 text-center">
                <div class="alert alert-info">
                    <i class="fas fa-info-circle"></i>
                    Aucune chambre disponible pour les critères sélectionnés.
                </div>
            </div>
        `;
        return;
    }

    container.innerHTML = rooms.map(room => `
        <div class="col-lg-4 col-md-6">
            <div class="card room-card">
                <div class="room-image" style="background-image: url('${room.image}')">
                    <span class="room-badge">${room.type}</span>
                </div>
                <div class="card-body">
                    <h5 class="card-title">${room.name}</h5>
                    <p class="card-text">${room.description}</p>
                    
                    <ul class="room-features">
                        <li><i class="fas fa-users"></i> ${room.maxOccupancy} personnes max</li>
                        <li><i class="fas fa-bed"></i> ${room.bedType}</li>
                        <li><i class="fas fa-expand"></i> ${room.size} m²</li>
                        <li><i class="fas fa-wifi"></i> WiFi gratuit</li>
                        ${room.hasBalcony ? '<li><i class="fas fa-leaf"></i> Balcon</li>' : ''}
                        ${room.hasJacuzzi ? '<li><i class="fas fa-hot-tub"></i> Jacuzzi</li>' : ''}
                    </ul>
                    
                    <div class="d-flex justify-content-between align-items-center mt-3">
                        <div class="room-price">
                            €${room.price}
                            <small>/nuit</small>
                        </div>
                        <span class="availability-status ${room.availability}">
                            ${getAvailabilityText(room.availability)}
                        </span>
                    </div>
                    
                    <div class="mt-3">
                        <button class="btn btn-primary w-100" onclick="openReservationModal(${room.id})" 
                                ${room.availability === 'unavailable' ? 'disabled' : ''}>
                            <i class="fas fa-calendar-check"></i> 
                            ${room.availability === 'unavailable' ? 'Non disponible' : 'Réserver'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

// Get availability text
function getAvailabilityText(availability) {
    switch(availability) {
        case 'available': return 'Disponible';
        case 'limited': return 'Dernières chambres';
        case 'unavailable': return 'Complet';
        default: return 'Disponible';
    }
}

// Load and display reviews
function loadReviews() {
    showLoading('reviewsContainer');
    
    // Simulate API call - replace with actual API endpoint
    setTimeout(() => {
        const reviews = getSampleReviews();
        displayReviews(reviews);
        currentReviews = reviews;
    }, 800);
}

// Load more reviews
function loadMoreReviews() {
    reviewsOffset += reviewsLimit;
    
    // Simulate loading more reviews
    setTimeout(() => {
        const moreReviews = getSampleReviews(reviewsOffset);
        if (moreReviews.length > 0) {
            appendReviews(moreReviews);
        }
    }, 500);
}

// Display reviews
function displayReviews(reviews) {
    const container = document.getElementById('reviewsContainer');
    container.innerHTML = reviews.slice(0, reviewsLimit).map(review => createReviewCard(review)).join('');
}

// Append more reviews
function appendReviews(reviews) {
    const container = document.getElementById('reviewsContainer');
    container.innerHTML += reviews.map(review => createReviewCard(review)).join('');
}

// Create review card HTML
function createReviewCard(review) {
    return `
        <div class="col-lg-4 col-md-6">
            <div class="review-card fade-in-up">
                <div class="review-stars">
                    ${generateStars(review.rating)}
                </div>
                <p class="review-text">"${review.comment}"</p>
                <div class="review-author">${review.clientName}</div>
                <div class="review-date">${formatDate(review.date)}</div>
                ${review.stayDuration ? `<small class="text-muted">Séjour de ${review.stayDuration}</small>` : ''}
            </div>
        </div>
    `;
}

// Generate star rating HTML
function generateStars(rating) {
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            stars += '<i class="fas fa-star"></i>';
        } else if (i - 0.5 <= rating) {
            stars += '<i class="fas fa-star-half-alt"></i>';
        } else {
            stars += '<i class="far fa-star"></i>';
        }
    }
    return stars;
}

// Format date for display
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

// Open reservation modal
function openReservationModal(roomId) {
    const room = currentRooms.find(r => r.id === roomId);
    if (!room) return;

    const checkIn = document.getElementById('checkInDate').value;
    const checkOut = document.getElementById('checkOutDate').value;
    const adults = document.getElementById('adults').value;
    const children = document.getElementById('children').value;

    if (!checkIn || !checkOut) {
        alert('Veuillez d\'abord sélectionner vos dates de séjour');
        return;
    }

    // Calculate number of nights and total price
    const nights = Math.ceil((new Date(checkOut) - new Date(checkIn)) / (1000 * 60 * 60 * 24));
    const totalPrice = room.price * nights;

    const modalContent = `
        <div class="row">
            <div class="col-md-6">
                <img src="${room.image}" alt="${room.name}" class="img-fluid rounded">
            </div>
            <div class="col-md-6">
                <h4>${room.name}</h4>
                <p>${room.description}</p>
                
                <div class="reservation-details">
                    <div class="row mb-2">
                        <div class="col-6"><strong>Arrivée:</strong></div>
                        <div class="col-6">${formatDate(checkIn)}</div>
                    </div>
                    <div class="row mb-2">
                        <div class="col-6"><strong>Départ:</strong></div>
                        <div class="col-6">${formatDate(checkOut)}</div>
                    </div>
                    <div class="row mb-2">
                        <div class="col-6"><strong>Durée:</strong></div>
                        <div class="col-6">${nights} nuit${nights > 1 ? 's' : ''}</div>
                    </div>
                    <div class="row mb-2">
                        <div class="col-6"><strong>Occupants:</strong></div>
                        <div class="col-6">${adults} adulte${adults > 1 ? 's' : ''}${children > 0 ? `, ${children} enfant${children > 1 ? 's' : ''}` : ''}</div>
                    </div>
                    <div class="row mb-3">
                        <div class="col-6"><strong>Prix par nuit:</strong></div>
                        <div class="col-6">€${room.price}</div>
                    </div>
                    <hr>
                    <div class="row mb-3">
                        <div class="col-6"><strong>Total:</strong></div>
                        <div class="col-6"><strong class="text-primary">€${totalPrice}</strong></div>
                    </div>
                </div>

                <div class="payment-options">
                    <div class="payment-option" onclick="selectPaymentOption('now')">
                        <i class="fas fa-credit-card"></i>
                        <div><strong>Payer maintenant</strong></div>
                        <small>Paiement sécurisé immédiat</small>
                    </div>
                    <div class="payment-option" onclick="selectPaymentOption('later')">
                        <i class="fas fa-clock"></i>
                        <div><strong>Payer plus tard</strong></div>
                        <small>Réservation sans paiement</small>
                    </div>
                </div>

                <div class="mt-3">
                    <button class="btn btn-primary w-100" onclick="processReservation(${roomId}, '${checkIn}', '${checkOut}', ${adults}, ${children}, ${totalPrice})">
                        <i class="fas fa-check"></i> Confirmer la réservation
                    </button>
                </div>
            </div>
        </div>
    `;

    document.getElementById('reservationForm').innerHTML = modalContent;
    const modal = new bootstrap.Modal(document.getElementById('reservationModal'));
    modal.show();
}

// Select payment option
function selectPaymentOption(option) {
    document.querySelectorAll('.payment-option').forEach(el => {
        el.classList.remove('selected');
    });
    event.target.closest('.payment-option').classList.add('selected');
}

// Process reservation
function processReservation(roomId, checkIn, checkOut, adults, children, totalPrice) {
    const selectedPayment = document.querySelector('.payment-option.selected');
    if (!selectedPayment) {
        alert('Veuillez sélectionner une option de paiement');
        return;
    }

    const paymentOption = selectedPayment.onclick.toString().includes('now') ? 'now' : 'later';
    
    // Show loading state
    const button = event.target;
    const originalText = button.innerHTML;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Traitement...';
    button.disabled = true;

    // Simulate reservation process
    setTimeout(() => {
        if (paymentOption === 'now') {
            // Redirect to payment page or open payment modal
            alert('Redirection vers le paiement sécurisé...');
            // window.location.href = '/payment?reservation=' + btoa(JSON.stringify({roomId, checkIn, checkOut, adults, children, totalPrice}));
        } else {
            // Show confirmation message
            showReservationConfirmation();
        }
        
        // Close modal
        bootstrap.Modal.getInstance(document.getElementById('reservationModal')).hide();
        
        // Reset button
        button.innerHTML = originalText;
        button.disabled = false;
    }, 2000);
}

// Show reservation confirmation
function showReservationConfirmation() {
    const alertHtml = `
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="fas fa-check-circle"></i>
            <strong>Réservation confirmée!</strong> Vous recevrez un email de confirmation sous peu.
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    document.body.insertAdjacentHTML('afterbegin', alertHtml);
    
    // Scroll to top to show the alert
    window.scrollTo({ top: 0, behavior: 'smooth' });
    
    // Auto-remove alert after 5 seconds
    setTimeout(() => {
        const alert = document.querySelector('.alert-success');
        if (alert) {
            alert.remove();
        }
    }, 5000);
}

// Show loading spinner
function showLoading(containerId) {
    const container = document.getElementById(containerId);
    container.innerHTML = `
        <div class="col-12">
            <div class="loading">
                <div class="spinner-border" role="status">
                    <span class="visually-hidden">Chargement...</span>
                </div>
                <p class="mt-3">Chargement en cours...</p>
            </div>
        </div>
    `;
}

// Sample data functions (replace with actual API calls)
function getSampleRooms() {
    return [
        {
            id: 1,
            name: "Chambre Deluxe",
            type: "DELUXE",
            description: "Chambre spacieuse avec vue sur la montagne, parfaite pour un séjour romantique.",
            price: 189,
            maxOccupancy: 2,
            bedType: "Lit king-size",
            size: 35,
            image: "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?ixlib=rb-4.0.3",
            availability: "available",
            hasBalcony: true,
            hasJacuzzi: false
        },
        {
            id: 2,
            name: "Suite Présidentielle",
            type: "SUITE",
            description: "Notre suite la plus luxueuse avec salon séparé, jacuzzi et vue panoramique.",
            price: 450,
            maxOccupancy: 4,
            bedType: "Lit king-size + canapé-lit",
            size: 85,
            image: "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?ixlib=rb-4.0.3",
            availability: "limited",
            hasBalcony: true,
            hasJacuzzi: true
        },
        {
            id: 3,
            name: "Chambre Standard",
            type: "STANDARD",
            description: "Chambre confortable avec toutes les commodités essentielles pour un séjour agréable.",
            price: 129,
            maxOccupancy: 2,
            bedType: "Lit double",
            size: 25,
            image: "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?ixlib=rb-4.0.3",
            availability: "available",
            hasBalcony: false,
            hasJacuzzi: false
        },
        {
            id: 4,
            name: "Chambre Familiale",
            type: "FAMILY",
            description: "Spacieuse chambre familiale avec lits superposés, idéale pour les familles.",
            price: 229,
            maxOccupancy: 6,
            bedType: "Lit double + lits superposés",
            size: 45,
            image: "https://images.unsplash.com/photo-1560472355-536de3962603?ixlib=rb-4.0.3",
            availability: "available",
            hasBalcony: true,
            hasJacuzzi: false
        },
        {
            id: 5,
            name: "Chambre Superior",
            type: "SUPERIOR",
            description: "Chambre élégante avec décoration raffinée et équipements modernes.",
            price: 159,
            maxOccupancy: 2,
            bedType: "Lit queen-size",
            size: 30,
            image: "https://images.unsplash.com/photo-1566665797739-1674de7a421a?ixlib=rb-4.0.3",
            availability: "unavailable",
            hasBalcony: false,
            hasJacuzzi: false
        },
        {
            id: 6,
            name: "Suite Junior",
            type: "JUNIOR_SUITE",
            description: "Suite avec coin salon et vue imprenable sur la vallée.",
            price: 289,
            maxOccupancy: 3,
            bedType: "Lit king-size + fauteuil-lit",
            size: 55,
            image: "https://images.unsplash.com/photo-1587985064135-0366536eab42?ixlib=rb-4.0.3",
            availability: "limited",
            hasBalcony: true,
            hasJacuzzi: true
        }
    ];
}

function getSampleReviews(offset = 0) {
    const allReviews = [
        {
            id: 1,
            clientName: "Marie Dubois",
            rating: 5,
            comment: "Séjour absolument magique ! L'hôtel est somptueux et le service irréprochable. La vue depuis notre chambre était à couper le souffle.",
            date: "2024-12-15",
            stayDuration: "3 nuits",
            status: "VALIDATED"
        },
        {
            id: 2,
            clientName: "Jean-Pierre Martin",
            rating: 4.5,
            comment: "Excellent hôtel avec un cadre exceptionnel. Le spa est fantastique et le restaurant propose une cuisine raffinée. Nous reviendrons !",
            date: "2024-12-10",
            stayDuration: "5 nuits",
            status: "VALIDATED"
        },
        {
            id: 3,
            clientName: "Sophie Laurent",
            rating: 5,
            comment: "Un week-end parfait dans un cadre idyllique. L'accueil était chaleureux et notre suite était magnifique. Merci pour ces moments inoubliables.",
            date: "2024-12-08",
            stayDuration: "2 nuits",
            status: "VALIDATED"
        },
        {
            id: 4,
            clientName: "Paul Bernaud",
            rating: 4,
            comment: "Très bel hôtel avec une architecture impressionnante. Les chambres sont spacieuses et bien équipées. Seul bémol : le wifi un peu lent.",
            date: "2024-12-05",
            stayDuration: "4 nuits",
            status: "VALIDATED"
        },
        {
            id: 5,
            clientName: "Catherine Moreau",
            rating: 5,
            comment: "L'Overlook Hotel dépasse toutes les attentes ! Le service personnalisé, la qualité des prestations et la beauté des lieux en font un endroit unique.",
            date: "2024-12-01",
            stayDuration: "1 semaine",
            status: "VALIDATED"
        },
        {
            id: 6,
            clientName: "Michel Rousseau",
            rating: 4.5,
            comment: "Hôtel de grande classe dans un environnement exceptionnel. Le petit-déjeuner était délicieux et le personnel très attentionné.",
            date: "2024-11-28",
            stayDuration: "3 nuits",
            status: "VALIDATED"
        }
    ];

    return allReviews.slice(offset, offset + reviewsLimit);
}
