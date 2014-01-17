class Update < ActiveRecord::Base
  attr_accessible :comit_id, :level, :repository_id, :typ, :typ_id
  has_many :update_records, :dependent => :destroy
  belongs_to :repository
  belongs_to :comit

  validates :level, numericality: { only_integer: true }, :presence => true, :inclusion => { :in => [ 0, 1, 2 ] }
  validates :typ, numericality: { only_integer: true }, :presence => true, :inclusion => { :in => [ 0, 1, 2 ] }
  validates :typ_id, numericality: { only_integer: true }
  validates :comit_id, presence: true
  validate :typ_validity
  validate :repository_validity
  validate :comit_validity
  validate :update_uniqueness

  def typ_validity
    arr = [ "Group", "School", "Machine" ]
    if !Kernel.const_get(arr[typ]).exists?(typ_id)
      errors.add(:typ_id, "Please choose an appropriate " + arr[typ] + ".")
    end
  end

  def repository_validity
    if !repository_id || !Repository.exists?(repository_id)
      errors.add(:repository_id, "Please choose an appropriate Repository for the commit.")
    end
  end

  def comit_validity
    if !comit_id || Comit.find(comit_id).repository_id != repository_id || !Comit.find(comit_id).is_update
      errors.add(:comit_id, "Please choose a valid commit for the repository that is an update.")
    end
  end

  def update_uniqueness
    update = Update.where(:repository_id => repository_id, :comit_id => comit_id, :typ => typ, :typ_id => typ_id)
    if !update.empty?
      errors.add(:comit_id, "A similar update already exists")
    end
  end
end
