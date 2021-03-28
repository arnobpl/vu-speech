from django.urls import path, include
from rest_framework import routers
from rest_framework_expiring_authtoken.views import obtain_expiring_auth_token

from . import views

router = routers.DefaultRouter()

router.register(r'signup', views.SignupViewSet, 'signup')
router.register(r'profile', views.ProfileViewSet, 'profile')
router.register(r'password', views.PasswordChangeViewSet, 'password_change')

urlpatterns = [
    path('api-token-auth/', obtain_expiring_auth_token, name='api_token_auth'),
    path('', include(router.urls)),
]
