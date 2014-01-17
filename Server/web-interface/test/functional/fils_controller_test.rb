require 'test_helper'

class FilsControllerTest < ActionController::TestCase
  setup do
    @fil = fils(:one)
  end

  test "should get index" do
    get :index
    assert_response :success
    assert_not_nil assigns(:fils)
  end

  test "should get new" do
    get :new
    assert_response :success
  end

  test "should create fil" do
    assert_difference('Fil.count') do
      post :create, fil: { commit_id: @fil.commit_id, filename: @fil.filename, size: @fil.size }
    end

    assert_redirected_to fil_path(assigns(:fil))
  end

  test "should show fil" do
    get :show, id: @fil
    assert_response :success
  end

  test "should get edit" do
    get :edit, id: @fil
    assert_response :success
  end

  test "should update fil" do
    put :update, id: @fil, fil: { commit_id: @fil.commit_id, filename: @fil.filename, size: @fil.size }
    assert_redirected_to fil_path(assigns(:fil))
  end

  test "should destroy fil" do
    assert_difference('Fil.count', -1) do
      delete :destroy, id: @fil
    end

    assert_redirected_to fils_path
  end
end
