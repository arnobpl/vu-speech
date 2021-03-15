from django.urls import path, include

from . import views

urlpatterns = [
    path('signup/', views.SignUpView.as_view(), name='signup'),
    path('profile/', views.ProfileUpdateView.as_view(), name='profile'),
    path('password/', views.PasswordChangeView.as_view(), name='password_change'),
    path('', include('django.contrib.auth.urls')),
]
