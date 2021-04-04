from django.urls import path, include
from rest_framework import routers

from . import views

router = routers.DefaultRouter()

router.register(r'signup', views.SignupViewSet, 'signup')
router.register(r'profile', views.ProfileViewSet, 'profile')
router.register(r'password', views.PasswordChangeViewSet, 'password_change')

urlpatterns = [
    path('api-token-auth/', views.ObtainExpiringAuthTokenWithLastLoginUpdate.as_view(), name='api_token_auth'),
    path('', include(router.urls)),
]
