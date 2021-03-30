from django.contrib import messages
from django.contrib.auth import update_session_auth_hash
from django.contrib.auth.forms import PasswordChangeForm
from django.contrib.auth.mixins import LoginRequiredMixin
from django.shortcuts import redirect, render
from django.urls import reverse_lazy
from django.views import generic
from rest_framework import mixins, viewsets, status
from rest_framework.permissions import AllowAny, IsAuthenticated
from rest_framework.response import Response

from . import forms, serializers


# Create your views here.


class SignupView(generic.CreateView):
    form_class = forms.UserCreationForm
    success_url = reverse_lazy('login')
    template_name = 'form.html'
    input_value = 'sign Up'

    def get_context_data(self, **kwargs):
        context = super().get_context_data(**kwargs)
        context['input_value'] = self.input_value
        return context


class SignupViewSet(mixins.CreateModelMixin,
                    viewsets.GenericViewSet):
    permission_classes = [AllowAny]
    serializer_class = serializers.SignupSerializer


class ProfileView(LoginRequiredMixin,
                  generic.UpdateView):
    form_class = forms.UserChangeForm
    template_name = 'form.html'
    input_value = 'update'

    def post(self, request, *args, **kwargs):
        form = forms.UserChangeForm(request.POST, instance=request.user)
        if form.is_valid():
            form.save()
            messages.success(request, 'Your profile was successfully updated!')
            return redirect('/')
        else:
            messages.error(request, 'Please correct the error below.')

    def get(self, request, **kwargs):
        form = forms.UserChangeForm(instance=request.user)
        return render(request, self.template_name, {'form': form, 'input_value': self.input_value})


class ProfileViewSet(mixins.RetrieveModelMixin,
                     mixins.UpdateModelMixin,
                     viewsets.GenericViewSet):
    permission_classes = [IsAuthenticated]
    serializer_class = serializers.ProfileSerializer

    def get_object(self):
        user = self.request.user
        return user


class PasswordChangeView(LoginRequiredMixin,
                         generic.UpdateView):
    form_class = PasswordChangeForm
    template_name = 'form.html'
    input_value = 'update password'

    def post(self, request, *args, **kwargs):
        form = PasswordChangeForm(request.user, request.POST)
        if form.is_valid():
            user = form.save()
            update_user_after_password_change(request, user)
            messages.success(request, 'Your password was successfully updated!')
            return redirect('/')
        else:
            messages.error(request, 'Please correct the error below.')

    def get(self, request, **kwargs):
        form = PasswordChangeForm(request.user)
        return render(request, self.template_name, {'form': form, 'input_value': self.input_value})


class PasswordChangeViewSet(mixins.UpdateModelMixin,
                            viewsets.GenericViewSet):
    permission_classes = [IsAuthenticated]

    def get_object(self):
        user = self.request.user
        return user

    def update(self, request, *args, **kwargs):
        user = self.get_object()
        serializer = serializers.PasswordChangeSerializer(data=request.data)

        if serializer.is_valid():
            old_password = serializer.data.get('old_password')
            if not user.check_password(old_password):
                return Response({'old_password': ['Wrong password.']},
                                status=status.HTTP_400_BAD_REQUEST)

            user.set_password(serializer.data.get('new_password'))
            user.save()
            update_user_after_password_change(request, user)
            return Response(status=status.HTTP_204_NO_CONTENT)

        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


def update_user_after_password_change(request, user):
    update_session_auth_hash(request, user)
    if hasattr(user, 'auth_token'):
        user.auth_token.delete()
