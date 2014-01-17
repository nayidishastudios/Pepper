require 'test_helper'

class InstallersControllerTest < ActionController::TestCase
  setup do
    @installer = installers(:one)
  end

  test "should get index" do
    get :index
    assert_response :success
    assert_not_nil assigns(:installers)
  end

  test "should get new" do
    get :new
    assert_response :success
  end

  test "should create installer" do
    assert_difference('Installer.count') do
      post :create, installer: { contact: @installer.contact, name: @installer.name, password_digest: @installer.password_digest, username: @installer.username }
    end

    assert_redirected_to installer_path(assigns(:installer))
  end

  test "should show installer" do
    get :show, id: @installer
    assert_response :success
  end

  test "should get edit" do
    get :edit, id: @installer
    assert_response :success
  end

  test "should update installer" do
    put :update, id: @installer, installer: { contact: @installer.contact, name: @installer.name, password_digest: @installer.password_digest, username: @installer.username }
    assert_redirected_to installer_path(assigns(:installer))
  end

  test "should destroy installer" do
    assert_difference('Installer.count', -1) do
      delete :destroy, id: @installer
    end

    assert_redirected_to installers_path
  end
end
