class UpdateRecordsController < ApplicationController
  # GET /update_records
  # GET /update_records.json
  def index
    @update_records = UpdateRecord.all.paginate(:page => params[:page], :per_page => 30)

    respond_to do |format|
      format.html # index.html.erb
      format.json { render json: @update_records }
    end
  end

  # GET /update_records/1
  # GET /update_records/1.json
  def show
    @update_record = UpdateRecord.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.json { render json: @update_record }
    end
  end

  # GET /update_records/new
  # GET /update_records/new.json
  def new
    @update_record = UpdateRecord.new

    respond_to do |format|
      format.html # new.html.erb
      format.json { render json: @update_record }
    end
  end

  # GET /update_records/1/edit
  def edit
    @update_record = UpdateRecord.find(params[:id])
  end

  # POST /update_records
  # POST /update_records.json
  def create
    @update_record = UpdateRecord.new(params[:update_record])

    respond_to do |format|
      if @update_record.save
        format.html { redirect_to @update_record, notice: 'Update record was successfully created.' }
        format.json { render json: @update_record, status: :created, location: @update_record }
      else
        format.html { render action: "new" }
        format.json { render json: @update_record.errors, status: :unprocessable_entity }
      end
    end
  end

  # PUT /update_records/1
  # PUT /update_records/1.json
  def update
    @update_record = UpdateRecord.find(params[:id])

    respond_to do |format|
      if @update_record.update_attributes(params[:update_record])
        format.html { redirect_to @update_record, notice: 'Update record was successfully updated.' }
        format.json { head :no_content }
      else
        format.html { render action: "edit" }
        format.json { render json: @update_record.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /update_records/1
  # DELETE /update_records/1.json
  def destroy
    @update_record = UpdateRecord.find(params[:id])
    @update_record.destroy

    respond_to do |format|
      format.html { redirect_to update_records_url }
      format.json { head :no_content }
    end
  end
end
