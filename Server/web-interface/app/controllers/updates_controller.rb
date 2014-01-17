class UpdatesController < ApplicationController
  # GET /updates
  # GET /updates.json
  def index
    @updates = Update.all.paginate(:page => params[:page], :per_page => 30)

    respond_to do |format|
      format.html # index.html.erb
      format.json { render json: @updates }
    end
  end

  # GET /updates/1
  # GET /updates/1.json
  def show
    @update = Update.find(params[:id])

    respond_to do |format|
      format.html # show.html.erb
      format.json {
        machines = []
        if @update.typ == 0
          group = Group.find(@update.typ_id)
          schools = School.where(:group_id => @update.typ_id)
          schools.map {|school|
            ms = Machine.where(:school_id => school.id)
            ms.map {|machine|
              machine[:name] = machine[:name] + "(" + school.name + "/" + group.name + ")"
              machines << machine
            }
          }
        elsif @update.typ == 1
          machines = Machine.where(:school_id => @update.typ_id)
          school = School.find(@update.typ_id)
          group = Group.find(school.group_id)
          machines.map {|machine|
            machine[:name] = machine[:name] + "(" + school.name + "/" + group.name + ")"
          }
        else
          machine = Machine.find(@update.typ_id)
          school = School.find(machine.school_id)
          group = Group.find(school.group_id)
          machine[:name] = machine[:name] + "(" + school.name + "/" + group.name + ")"
          machines = [ machine ]
        end
        @update[:machines] = machines
        render json: @update
      }
    end
  end

  # GET /updates/new
  # GET /updates/new.json
  def new
    @update = Update.new

    respond_to do |format|
      format.html # new.html.erb
      format.json { render json: @update }
    end
  end

  # GET /updates/1/edit
  def edit
    @form_for_update = true
    @update = Update.find(params[:id])
  end

  # POST /updates
  # POST /updates.json
  def create
    puts params
    success = true
    @form_for_update = false

    params[:update][:typ_id].each do |typ_id|
      if (!typ_id.empty?)
        update = Update.new(:level => params[:update][:level], :typ => params[:update][:typ], :typ_id => typ_id,:repository_id => params[:update][:repository_id], :comit_id => params[:update][:comit_id])
        if update.valid?
          update.save
        else
          success = false
          @errors += update.errors
        end
      end
    end

    respond_to do |format|
      if success
        format.html {
          flash[:notice] = 'Update was successfully created.'
          redirect_to :controller => 'updates'
        }
        format.json { render json: {}, status: :created }
      else
        format.html { render action: "new" }
        format.json { render json: @errors, status: :unprocessable_entity }
      end
    end
  end

  # PUT /updates/1
  # PUT /updates/1.json
  def update
    @update = Update.find(params[:id])

    respond_to do |format|
      if @update.update_attributes(params[:update])
        format.html { redirect_to @update, notice: 'Update was successfully updated.' }
        format.json { head :no_content }
      else
        format.html { render action: "edit" }
        format.json { render json: @update.errors, status: :unprocessable_entity }
      end
    end
  end

  # DELETE /updates/1
  # DELETE /updates/1.json
  def destroy
    @update = Update.find(params[:id])
    @update.destroy

    respond_to do |format|
      format.html { redirect_to updates_url }
      format.json { head :no_content }
    end
  end
end
