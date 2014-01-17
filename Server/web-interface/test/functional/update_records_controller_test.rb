require 'test_helper'

class UpdateRecordsControllerTest < ActionController::TestCase
  setup do
    @update_record = update_records(:one)
  end

  test "should get index" do
    get :index
    assert_response :success
    assert_not_nil assigns(:update_records)
  end

  test "should get new" do
    get :new
    assert_response :success
  end

  test "should create update_record" do
    assert_difference('UpdateRecord.count') do
      post :create, update_record: { machine_id: @update_record.machine_id, update_id: @update_record.update_id }
    end

    assert_redirected_to update_record_path(assigns(:update_record))
  end

  test "should show update_record" do
    get :show, id: @update_record
    assert_response :success
  end

  test "should get edit" do
    get :edit, id: @update_record
    assert_response :success
  end

  test "should update update_record" do
    put :update, id: @update_record, update_record: { machine_id: @update_record.machine_id, update_id: @update_record.update_id }
    assert_redirected_to update_record_path(assigns(:update_record))
  end

  test "should destroy update_record" do
    assert_difference('UpdateRecord.count', -1) do
      delete :destroy, id: @update_record
    end

    assert_redirected_to update_records_path
  end
end
